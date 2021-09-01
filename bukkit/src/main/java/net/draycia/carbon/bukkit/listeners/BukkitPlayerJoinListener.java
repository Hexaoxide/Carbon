package net.draycia.carbon.bukkit.listeners;

import com.google.inject.Inject;
import java.util.Objects;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.bukkit.util.BukkitCapabilities;
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
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BukkitPlayerJoinListener implements Listener {

    private final CarbonChat carbonChat;

    @Inject
    public BukkitPlayerJoinListener(final CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerJoinEvent event) {
        this.carbonChat.server()
            .player(event.getPlayer().getUniqueId()).thenAccept(result -> {
                if (result.player() == null) {
                    return;
                }

                final CarbonPlayer player = result.player();

                Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) this.carbonChat, () -> {
                    if (player.displayName() != null) {
                        player.displayName(player.displayName());
                    } else {
                        final @Nullable Component nickname = this.createDefaultNickname(player);

                        if (nickname != null) {
                            player.displayName(nickname);
                        }
                    }
                });
            });
    }

    private @Nullable Component createDefaultNickname(final CarbonPlayer player) {
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
