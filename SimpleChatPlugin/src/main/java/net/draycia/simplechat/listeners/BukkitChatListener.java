package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class BukkitChatListener implements Listener {

    private SimpleChat simpleChat;

    public BukkitChatListener(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    // Chat messages
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        event.getRecipients().clear();

        ChatUser user = simpleChat.getUserService().wrap(event.getPlayer());

        if (event.isAsynchronous()) {
            user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                user.getSelectedChannel().sendMessage(user, event.getMessage(), false);
            });
        }
    }

}
