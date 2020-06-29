package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private SimpleChat simpleChat;

    public PlayerChatListener(SimpleChat simpleChat) {
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

}
