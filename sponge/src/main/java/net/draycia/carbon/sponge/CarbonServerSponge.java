package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import static net.kyori.adventure.text.Component.text;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerSponge implements CarbonServer, ForwardingAudience.Single {

    private final Game game;
    private final UserManager<CarbonPlayerCommon> userManager;

    @Inject
    private CarbonServerSponge(final UserManager<CarbonPlayerCommon> userManager, final Game game) {
        this.game = game;
        this.userManager = userManager;
    }

    @Override
    public @NotNull Audience audience() {
        return this.game.server();
    }

    @Override
    public Audience console() {
        return this.game.systemSubject();
    }

    @Override
    public List<CarbonPlayerSponge> players() {
        final var players = new ArrayList<CarbonPlayerSponge>();

        for (final var player : Sponge.server().onlinePlayers()) {
            final ComponentPlayerResult<CarbonPlayer> result = this.player(player).join();

            if (result.player() != null) {
                players.add((CarbonPlayerSponge) result.player());
            }
        }

        return players;
    }

    private CompletableFuture<ComponentPlayerResult<CarbonPlayer>> wrapPlayer(final UUID uuid) {
        return this.userManager.carbonPlayer(uuid).thenCompose(result -> {
            return CompletableFuture.supplyAsync(() -> {
                if (result.player() != null) {
                    new ComponentPlayerResult<>(new CarbonPlayerSponge(result.player()), Component.empty());
                }

                final @Nullable String name = this.resolveName(uuid).join();

                if (name != null) {
                    final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

                    return new ComponentPlayerResult<>(new CarbonPlayerSponge(player), Component.empty());
                }

                return new ComponentPlayerResult<>(null, text("Name not found for uuid!"));
            });
        });
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayer>> player(final UUID uuid) {
        return this.wrapPlayer(uuid);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayer>> player(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable UUID uuid = this.resolveUUID(username).join();

            if (uuid != null) {
                return this.player(uuid).join();
            }

            return new ComponentPlayerResult<>(null, text("No UUID found for name."));
        });
    }

    public CompletableFuture<ComponentPlayerResult<CarbonPlayer>> player(final Player player) {
        return this.player(player.uniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        // TODO: user cache?
        return CompletableFuture.supplyAsync(() ->
            Sponge.server().gameProfileManager().basicProfile(username).join().uuid()
        );
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
            Sponge.server().gameProfileManager().basicProfile(uuid).join().name().orElse(null)
        );
    }

}
