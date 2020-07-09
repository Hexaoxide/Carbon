package net.draycia.simplechat.listeners;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class FilterHandler implements Listener {

    private SimpleChat simpleChat;

    public FilterHandler(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFilter(ChatFormatEvent event) {
        ChatChannel channel = event.getChatChannel();
        String filterText = simpleChat.getConfig().getString("filter-text", "****");

        if (channel.filterEnabled() && simpleChat.getConfig().contains("filters")) {
            for (String entry : simpleChat.getConfig().getStringList("filters")) {
                event.setMessage(event.getMessage().replaceAll(entry, filterText));
            }
        }
    }

}
