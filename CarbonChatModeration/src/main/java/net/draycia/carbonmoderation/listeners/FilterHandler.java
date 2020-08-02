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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                Matcher matcher;

                if (config.getBoolean("filters.case-sensitive")) {
                    matcher = Pattern.compile(word).matcher(event.getMessage());
                } else {
                    matcher = Pattern.compile(word, Pattern.CASE_INSENSITIVE).matcher(event.getMessage());
                }

                if (replacement.equals("_")) {
                    event.setMessage(matcher.replaceAll(""));
                } else {
                    event.setMessage(matcher.replaceAll(replacement));
                }
            }
        }

        for (String blockedWord : config.getStringList("filters.blocked-words")) {
            if (Pattern.compile(blockedWord, Pattern.CASE_INSENSITIVE).matcher(event.getMessage()).find()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean channelUsesFilter(ChatChannel chatChannel) {
        Object filter = chatChannel.getContext("filter");

        return filter instanceof Boolean && ((Boolean) filter);
    }

}
