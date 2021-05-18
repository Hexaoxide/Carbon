package net.draycia.carbon.bukkit;

import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
public class CarbonServerBukkit implements CarbonServer {

    private final CarbonChatBukkitEntry chatBukkitEntry;
    private final CarbonChatBukkit carbonChatBukkit;

    public CarbonServerBukkit(
        final CarbonChatBukkitEntry chatBukkitEntry,
        final CarbonChatBukkit carbonChatBukkit
    ) {
        this.chatBukkitEntry = chatBukkitEntry;
        this.carbonChatBukkit = carbonChatBukkit;
    }

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
        return this.carbonChatBukkit.userManager().carbonPlayer(uuid);
    }

    @Override
    public @Nullable CarbonPlayer player(final String username) {
        return this.carbonChatBukkit.userManager().carbonPlayer(username);
    }

    private @Nullable CarbonPlayer player(final Player player) {
        return this.player(player.getUniqueId());
    }

}
