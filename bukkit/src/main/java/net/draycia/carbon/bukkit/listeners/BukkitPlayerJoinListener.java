package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import java.util.Objects;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.bukkit.util.BukkitCapabilities;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BukkitPlayerJoinListener implements Listener {

    private final CarbonChat carbonChat;
    private final PrimaryConfig primaryConfig;

    @Inject
    public BukkitPlayerJoinListener(
        final CarbonChat carbonChat,
        final PrimaryConfig primaryConfig
    ) {
        this.carbonChat = carbonChat;
        this.primaryConfig = primaryConfig;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerJoinEvent event) {
        final ComponentPlayerResult result = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer player = result.player();

        // Apply temporary and non temporary display names
        if (player.hasCustomDisplayName()) {
            player.displayName(player.displayName());
        } else {
            final @Nullable Component nickname = this.createDefaultNickname(player);

            if (nickname != null) {
                player.temporaryDisplayName(nickname);
            }
        }

        // Don't show join messages when muted
        if (this.primaryConfig.hideMutedJoinLeaveQuit() && player.muted()) {
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // Early exit in case "hide muted join / leave messages when muted" is disabled
        if (!this.primaryConfig.hideMutedJoinLeaveQuit()) {
            return;
        }

        final ComponentPlayerResult result = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer player = result.player();

        // Don't show quit messages when muted
        if (player.muted()) {
            event.quitMessage(null);
        }
    }

    private @Nullable Component createDefaultNickname(final CarbonPlayer player) {
        // TODO: command to re-calculate and apply default nicknames
        if (BukkitCapabilities.vaultEnabled()) {
            final Chat vaultChat = Objects.requireNonNull(BukkitCapabilities.chat());
            final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());

            final @Nullable String format =
                vaultChat.getPlayerInfoString(bukkitPlayer, "autonick", null);

            if (format != null) {
                return MiniMessage.get().parse(format, Template.of("username", player.username()));
            }
        }

        return null;
    }

}
