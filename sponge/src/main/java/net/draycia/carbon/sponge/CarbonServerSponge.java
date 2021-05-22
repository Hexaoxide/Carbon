package net.draycia.carbon.sponge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonServerSponge implements CarbonServer {

    @Inject
    private CarbonChatSponge carbonChatSponge;

    @Override
    public Audience console() {
        return Sponge.systemSubject();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : Sponge.server().onlinePlayers()) {
            final @Nullable CarbonPlayer carbonPlayer = this.player(player);

            if (carbonPlayer != null) {
                players.add(carbonPlayer);
            }
        }

        return players;
    }

    @Override
    public @Nullable CarbonPlayer player(final UUID uuid) {
        return this.carbonChatSponge.server().player(uuid);
    }

    @Override
    public @Nullable CarbonPlayer player(final String username) {
        return this.carbonChatSponge.server().player(username);
    }

    private @Nullable CarbonPlayer player(final Player player) {
        return this.player(player.uniqueId());
    }

}
