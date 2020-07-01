package net.draycia.simplechat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.managers.UserManager;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.javacord.api.DiscordApi;


public class PlayerListener implements Listener {

    private SimpleChat simpleChat;

    public PlayerListener(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Chat messages
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        ChatUser user = UserManager.wrap(event.getPlayer());

        if (event.isAsynchronous()) {
            user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
            });
        }
    }

    // Player joins
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!simpleChat.getConfig().getBoolean("listeners.join")) {
            return;
        }

        ChatChannel chatChannel = simpleChat.getDefaultChannel();

        if (chatChannel != null && chatChannel.getChannelId() > -1) {
            DiscordApi api = simpleChat.getDiscordManager().getDiscordAPI();
            String message = simpleChat.getConfig().getString("language.player-joined");

            api.getServerTextChannelById(chatChannel.getChannelId()).ifPresent(channel -> {
                channel.sendMessage(PlaceholderAPI.setPlaceholders(event.getPlayer(), message));
            });
        }
    }

    // Player quits
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!simpleChat.getConfig().getBoolean("listeners.leave")) {
            return;
        }

        ChatChannel chatChannel = simpleChat.getDefaultChannel();

        if (chatChannel != null && chatChannel.getChannelId() > -1) {
            DiscordApi api = simpleChat.getDiscordManager().getDiscordAPI();
            String message = simpleChat.getConfig().getString("language.player-left");

            api.getServerTextChannelById(chatChannel.getChannelId()).ifPresent(channel -> {
                channel.sendMessage(PlaceholderAPI.setPlaceholders(event.getPlayer(), message));
            });
        }
    }

    // Player deaths
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!simpleChat.getConfig().getBoolean("listeners.death")) {
            return;
        }

        ChatChannel chatChannel = simpleChat.getDefaultChannel();

        if (chatChannel != null && chatChannel.getChannelId() > -1) {
            DiscordApi api = simpleChat.getDiscordManager().getDiscordAPI();

            api.getServerTextChannelById(chatChannel.getChannelId()).ifPresent(channel -> {
                channel.sendMessage(event.getDeathMessage());
            });
        }
    }

}
