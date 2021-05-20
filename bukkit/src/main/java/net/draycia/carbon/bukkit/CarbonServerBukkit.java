package net.draycia.carbon.bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.UUID;

@Singleton
@DefaultQualifier(NonNull.class)
public class CarbonServerBukkit implements CarbonServer {

    @Inject
    private CarbonChatBukkitEntry chatBukkitEntry;

    @Inject
    private UserManager userManager;

    @Override
    public Audience console() {
        return this.chatBukkitEntry.getServer().getConsoleSender();
    }

    @Override
    public Iterable<? extends CarbonPlayer> players() {
        final var players = new ArrayList<CarbonPlayer>();

        for (final var player : this.chatBukkitEntry.getServer().getOnlinePlayers()) {
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
        return this.player(player.getUniqueId());
    }

}
