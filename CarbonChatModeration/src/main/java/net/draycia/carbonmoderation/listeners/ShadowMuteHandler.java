package net.draycia.carbonmoderation.listeners;

import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShadowMuteHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onComponent(ChatComponentEvent event) {
        if (event.getSender().isShadowMuted()) {
            if (!event.getSender().equals(event.getTarget())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(ChatFormatEvent event) {
        if (event.getTarget() != null) {
            return;
        }

        if (!event.getSender().isShadowMuted()) {
            return;
        }

        event.setFormat("[SM] " + event.getFormat());
    }

}
