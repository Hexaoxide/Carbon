package net.draycia.carbonmoderation.listeners;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbonmoderation.CarbonChatModeration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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

        if (!channelUsesFilter(event.getChannel())) {
            return;
        }

        FileConfiguration config = moderation.getConfig();
        ConfigurationSection filters = config.getConfigurationSection("filters.filters");

        for (String replacement : filters.getKeys(false)) {
            List<String> filteredWords = filters.getStringList(replacement);

            for (String word : filteredWords) {
                event.setMessage(event.getMessage().replaceAll(word, replacement));
            }
        }
    }

    private boolean channelUsesFilter(ChatChannel chatChannel) {
        List<String> channels = moderation.getConfig().getStringList("filters.channels");

        return channels.contains("*") || channels.contains(chatChannel.getKey());
    }

}
