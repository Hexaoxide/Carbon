package net.draycia.carbonmoderation.listeners;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbonmoderation.CarbonChatModeration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class FilterHandler implements Listener {

    private CarbonChatModeration moderation;

    public FilterHandler(CarbonChatModeration moderation) {
        this.moderation = moderation;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFilter(ChatFormatEvent event) {
        if (!moderation.getConfig().getBoolean("filters.enabled")) {
            return;
        }

        String filterText = moderation.getConfig().getString("filters.filter-text", "****");

        if (channelUsesFilter(event.getChatChannel())) {
            for (String entry : moderation.getConfig().getStringList("filters.filtered-words")) {
                event.setMessage(event.getMessage().replaceAll(entry, filterText));
            }
        }
    }

    private boolean channelUsesFilter(ChatChannel chatChannel) {
        List<String> channels = moderation.getConfig().getStringList("filters.channels");

        return channels.contains("*") || channels.contains(chatChannel.getKey());
    }

}
