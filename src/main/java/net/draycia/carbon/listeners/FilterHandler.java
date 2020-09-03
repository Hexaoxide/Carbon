package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterHandler implements Listener {

    private final CarbonChat carbonChat;

    private final Map<String, List<Pattern>> patternReplacements = new HashMap<>();
    private final List<Pattern> blockedWords = new ArrayList<>();

    public FilterHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        reloadFilters();
    }

    public void reloadFilters() {
        patternReplacements.clear();
        blockedWords.clear();

        FileConfiguration config = carbonChat.getModConfig();
        ConfigurationSection filters = config.getConfigurationSection("filters.filters");

        for (String replacement : filters.getKeys(false)) {
            List<Pattern> patterns = new ArrayList<>();

            for (String word : filters.getStringList(replacement)) {
                if (carbonChat.getModConfig().getBoolean("filters.case-sensitive")) {
                    patterns.add(Pattern.compile(word));
                } else {
                    patterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
                }
            }

            patternReplacements.put(replacement, patterns);
        }

        for (String replacement : config.getStringList("filters.blocked-words")) {
            for (String word : filters.getStringList(replacement)) {
                if (carbonChat.getModConfig().getBoolean("filters.case-sensitive")) {
                    blockedWords.add(Pattern.compile(word));
                } else {
                    blockedWords.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFilter(PreChatFormatEvent event) {
        if (!carbonChat.getModConfig().getBoolean("filters.enabled")) {
            return;
        }

        if (event.getUser().isOnline()) {
            if (event.getUser().asPlayer().hasPermission("carbonchat.filter.exempt")) {
                return;
            }
        }

        if (!channelUsesFilter(event.getChannel())) {
            return;
        }

        String message = event.getMessage();
        //Matcher matcher;

        for (Map.Entry<String, List<Pattern>> entry : patternReplacements.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                Matcher matcher = pattern.matcher(message);

                if (entry.getKey().equals("_")) {
                    message = matcher.replaceAll("");
                } else {
                    message = matcher.replaceAll(entry.getKey());
                }
            }
        }

        event.setMessage(message);

        for (Pattern blockedWord : blockedWords) {
            if (blockedWord.matcher(event.getMessage()).find()) {
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
