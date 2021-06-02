package net.draycia.carbon.velocity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonServerVelocity implements CarbonServer, ForwardingAudience.Single {

    private final Map<UUID, CarbonPlayerVelocity> userCache = new ConcurrentHashMap<>();
    private final ProxyServer server;
    private final UserManager userManager;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Inject
    private CarbonServerVelocity(final ProxyServer server, final UserManager userManager) {
        this.server = server;
        this.userManager = userManager;
    }

    @Override
    public Audience audience() {
        return Audience.audience(this.console(), Audience.audience(this.players()));
    }

    @Override
    public Audience console() {
        return this.server.getConsoleCommandSource();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.server.getAllPlayers()) {
            final @Nullable CarbonPlayer carbonPlayer = this.player(player).join();

            if (carbonPlayer != null) {
                players.add(carbonPlayer);
            }
        }

        return players;
    }

    private CompletableFuture<@Nullable CarbonPlayerVelocity> loadPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final UserManager.PlayerResult result = this.userManager.carbonPlayer(uuid).join();

            if (result.successful()) {
                return new CarbonPlayerVelocity(this.server, result.player());
            }

            // TODO: replace this with some mojang call or smth
            final var profile = this.server.getPlayer(uuid).get().getGameProfile();

            return new CarbonPlayerVelocity(
                this.server,
                Identity.identity(uuid),
                profile.getName(),
                uuid
            );
        });
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            return this.userCache.computeIfAbsent(uuid, id -> {
                return this.loadPlayer(uuid).join();
            });
        });
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final String username) {
        for (final var velocityPlayer : this.userCache.values()) {
            if (velocityPlayer.username().equalsIgnoreCase(username)) {
                return CompletableFuture.completedFuture(velocityPlayer);
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            final @Nullable UUID uuid = this.resolveUUID(username).join();

            if (uuid != null) {
                return this.player(uuid).join();
            }

            return null;
        });
    }

    private CompletableFuture<@Nullable CarbonPlayer> player(final Player player) {
        return this.player(player.getUniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final Optional<Player> player = this.server.getPlayer(username);

            if (player.isPresent()) {
                return player.get().getUniqueId();
            }

            try {
                final HttpRequest request = HttpRequest
                    .newBuilder(new URI("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .GET()
                    .build();

                final HttpResponse<String> response =
                    this.client.send(request, HttpResponse.BodyHandlers.ofString());
                final String mojangResponse = response.body().replaceAll(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5");

                final JsonArray jsonArray = this.gson.fromJson(mojangResponse, JsonObject.class).getAsJsonArray();
                final JsonObject json = (JsonObject) jsonArray.get(1);
                final String uuid = json.get("uuid").getAsString();

                return UUID.fromString(uuid);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

}
