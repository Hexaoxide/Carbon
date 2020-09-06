package net.draycia.carbon.listeners;

import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MuteHandler implements Listener {

    @EventHandler
    public void onMute(@NonNull PreChatFormatEvent event) {
        if (event.getUser().isMuted()) {
            event.setCancelled(true);
        }
    }

}
