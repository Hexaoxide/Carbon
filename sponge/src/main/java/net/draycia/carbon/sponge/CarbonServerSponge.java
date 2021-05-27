package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerSponge implements CarbonServer {

    private final Game game;
    private final UserManager userManager;

    @Inject
    private CarbonServerSponge(final UserManager userManager, final Game game) {
        this.userManager = userManager;
        this.game = game;
    }

    @Override
    public Audience console() {
        return Sponge.systemSubject();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.game.server().onlinePlayers()) {
            final @Nullable CarbonPlayer carbonPlayer = this.player(player);

            if (carbonPlayer != null) {
                players.add(carbonPlayer);
            }
        }

        return players;
    }

    @Override
    public @Nullable CarbonPlayer player(final UUID uuid) {
        return this.userManager.carbonPlayer(uuid);
    }

    @Override
    public @Nullable CarbonPlayer player(final String username) {
        return this.userManager.carbonPlayer(username);
    }

    private @Nullable CarbonPlayer player(final Player player) {
        return this.player(player.uniqueId());
    }

}
