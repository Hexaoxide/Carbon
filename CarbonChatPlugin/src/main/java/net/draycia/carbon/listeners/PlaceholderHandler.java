package net.draycia.carbon.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlaceholderHandler implements Listener {

    private final CarbonChat carbonChat;

    public PlaceholderHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatPlaceholder(ChatFormatEvent event) {
        ConfigurationSection replacements = carbonChat.getConfig().getConfigurationSection("replacements");

        if (replacements != null) {
            for (String key : replacements.getKeys(false)) {
                event.setMessage(event.getMessage().replace(key, replacements.getString(key)));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPapiPlaceholder(ChatFormatEvent event) {
        event.setFormat(PlaceholderAPI.setPlaceholders(event.getUser().asOfflinePlayer(), event.getFormat()));
    }

}
