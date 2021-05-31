package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.ProfileNotFoundException;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerSponge implements CarbonServer, ForwardingAudience.Single {

    private final Map<UUID, CarbonPlayerSponge> userCache = new HashMap<>();
    private final Game game;
    private final UserManager userManager;

    @Inject
    private CarbonServerSponge(final UserManager userManager, final Game game) {
        this.userManager = userManager;
        this.game = game;
    }

    @Override
    public Audience audience() {
        return Audience.audience(this.console(), Audience.audience(this.players()));
    }

    @Override
    public Audience console() {
        return Sponge.systemSubject();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.game.server().onlinePlayers()) {
            final @Nullable CarbonPlayer carbonPlayer = this.player(player).join();

            if (carbonPlayer != null) {
                players.add(carbonPlayer);
            }
        }

        return players;
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final UUID uuid) {
        final CarbonPlayerSponge carbonPlayerSponge = this.userCache.get(uuid);

        if (carbonPlayerSponge != null) {
            return CompletableFuture.completedFuture(carbonPlayerSponge);
        }

        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayer carbonPlayer = this.userManager.carbonPlayer(uuid).join();

            if (carbonPlayer != null) {
                return new CarbonPlayerSponge(carbonPlayer);
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable CarbonPlayer> player(final String username) {
        for (final var spongePlayer : this.userCache.values()) {
            if (spongePlayer.username().equalsIgnoreCase(username)) {
                return CompletableFuture.completedFuture(spongePlayer);
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
        return this.player(player.uniqueId());
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final GameProfile profile;
            try {
                profile = Sponge.server().gameProfileManager().basicProfile(username).get();
                return profile.uuid();
            } catch (InterruptedException | ExecutionException | ProfileNotFoundException e) {
                return null;
            }
        });
    }

}
