package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {

    private SimpleChat simpleChat;

    public PlayerListener(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        if (event.isAsynchronous()) {
            simpleChat.getPlayerChannel(event.getPlayer()).sendMessage(event.getPlayer(), event.getMessage());
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                simpleChat.getPlayerChannel(event.getPlayer()).sendMessage(event.getPlayer(), event.getMessage());
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO: join messages (placeholderapi support)
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        // TODO: leave messages (placeholderapi support)
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // TODO: death messages (placeholderapi support)
    }

}
