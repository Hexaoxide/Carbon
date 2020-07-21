package net.draycia.simplechat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.events.ChatFormatEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlaceholderHandler implements Listener {

    private final SimpleChat simpleChat;

    public PlaceholderHandler(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatPlaceholder(ChatFormatEvent event) {
        ConfigurationSection replacements = simpleChat.getConfig().getConfigurationSection("replacements");

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
