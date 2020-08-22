package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

public class ItemLinkHandler implements Listener {

    private final CarbonChat carbonChat;

    public ItemLinkHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemLink(ChatComponentEvent event) {
        // Handle item linking placeholders
        if (event.getSender().isOnline()) {
            for (Pattern pattern : event.getChannel().getItemLinkPatterns()) {
                String patternContent = pattern.toString().replace("\\Q", "").replace("\\E", "");

                if (event.getOriginalMessage().contains(patternContent)) {
                    TextComponent component = event.getComponent().replaceFirst(pattern, (input) -> {
                        return TextComponent.builder().append(carbonChat.getItemStackUtils().createComponent(event.getSender().asPlayer()));
                    });

                    event.setComponent(component);
                    break;
                }
            }
        }
    }

}
