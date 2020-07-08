package net.draycia.simplechat.listeners.chat;

import net.draycia.simplechat.events.ChatComponentEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShadowMuteHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onComponent(ChatComponentEvent event) {
        if (event.getUser().isShadowMuted()) {
            if (event.getRecipients().size() == 1 && event.getUser().equals(event.getRecipients().get(0))) {
                return;
            }

            try {
                event.getRecipients().clear();
                event.getRecipients().add(event.getUser());
            } catch (UnsupportedOperationException e) {}
        }

        // TODO: figure out a way to add the console "[SM]" prefix for shadow muted players
    }

}
