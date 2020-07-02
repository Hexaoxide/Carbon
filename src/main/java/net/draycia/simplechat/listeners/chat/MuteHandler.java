package net.draycia.simplechat.listeners.chat;

import net.draycia.simplechat.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MuteHandler implements Listener {

    @EventHandler
    public void onMute(ChatFormatEvent event) {
        if (event.getUser().isMuted()) {
            event.setCancelled(true);
        }
    }

}
