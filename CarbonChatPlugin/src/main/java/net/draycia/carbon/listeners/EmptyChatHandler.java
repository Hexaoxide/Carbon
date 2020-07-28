package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EmptyChatHandler implements Listener {

    private final CarbonChat carbonChat;

    public EmptyChatHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onComponent(ChatComponentEvent event) {
        if (event.isCustomPlayerFormat()) {
            return;
        }

        if (event.getRecipients().size() == 1 && event.getUser().equals(event.getRecipients().get(0))) {
            String message = carbonChat.getConfig().getString("language.empty-channel");

            if (message == null || message.isEmpty() || message.equalsIgnoreCase("NONE")) {
                return;
            }

            event.getUser().sendMessage(carbonChat.getAdventureManager().processMessage(message));
        }
    }

}
