package net.draycia.carbonmoderation.listeners;

import net.draycia.carbon.events.ChatComponentEvent;
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

        // TODO: figure out a way to add the console "[SM]" prefix for shadow muted players
    }

}
