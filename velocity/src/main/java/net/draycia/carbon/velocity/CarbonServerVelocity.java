package net.draycia.carbon.velocity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.UuidUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class CarbonServerVelocity implements CarbonServer, ForwardingAudience.Single {

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
            final @Nullable ComponentPlayerResult carbonPlayer = this.player(player).join();

            if (carbonPlayer.player() != null) {
                players.add(carbonPlayer.player());
            }
        }

        return players;
    }

    private CompletableFuture<ComponentPlayerResult> wrapPlayer(final UUID uuid) {
        return this.userManager.carbonPlayer(uuid).thenCompose(result -> {
            return CompletableFuture.supplyAsync(() -> {
                if (result.player() != null) {
                    new ComponentPlayerResult(new CarbonPlayerVelocity(this.server, result.player()), Component.empty());
                }

                final @Nullable String name = this.resolveName(uuid).join();

                if (name != null) {
                    final CarbonPlayerCommon player = new CarbonPlayerCommon(null,
                        null, name, uuid);

                    return new ComponentPlayerResult(new CarbonPlayerVelocity(this.server, player), Component.empty());
                }

                return new ComponentPlayerResult(null, text("Name not found for uuid!"));
            });
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> player(final UUID uuid) {
        return this.wrapPlayer(uuid);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult> player(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable UUID uuid = this.resolveUUID(username).join();

            if (uuid != null) {
                return this.player(uuid).join();
            }

            return new ComponentPlayerResult(null, text("No UUID found for name."));
        });
    }

    public CompletableFuture<ComponentPlayerResult> player(final Player player) {
        return this.player(player.getUniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest request = HttpRequest
                    .newBuilder(new URI("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .GET()
                    .build();

                final HttpResponse<String> response =
                    this.client.send(request, HttpResponse.BodyHandlers.ofString());
                final String mojangResponse = response.body();

                final JsonArray jsonArray = this.gson.fromJson(mojangResponse, JsonObject.class).getAsJsonArray();
                final JsonObject json = (JsonObject) jsonArray.get(1);

                return UuidUtils.fromUndashed(json.get("uuid").getAsString());
            } catch (final URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest request = HttpRequest
                    .newBuilder(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + UuidUtils.toUndashed(uuid)))
                    .GET()
                    .build();

                final HttpResponse<String> response =
                    this.client.send(request, HttpResponse.BodyHandlers.ofString());
                final String mojangResponse = response.body();

                final JsonArray jsonArray = this.gson.fromJson(mojangResponse, JsonObject.class).getAsJsonArray();
                final JsonObject json = (JsonObject) jsonArray.get(1);

                return json.get("name").getAsString();
            } catch (final URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

}
