package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChatComponentEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EmptyChatHandler implements Listener {

    private final SimpleChat simpleChat;

    public EmptyChatHandler(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onComponent(ChatComponentEvent event) {
        if (event.isCustomPlayerFormat()) {
            return;
        }

        if (event.getRecipients().size() == 1 && event.getUser().equals(event.getRecipients().get(0))) {
            String message = simpleChat.getConfig().getString("language.empty-channel");

            if (message == null || message.isEmpty()) {
                return;
            }

            event.getUser().sendMessage(simpleChat.getAdventureManager().processMessage(message));
        }
    }

}
