package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatFormatEvent;
import net.draycia.carbon.managers.AdventureManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UserFormattingListener implements Listener {

    @EventHandler
    public void onFormat(ChatFormatEvent event) {

        if (!event.getUser().isOnline()) {
            suppressFormatting(event);
            return;
        }

        Player p = event.getUser().asPlayer();
        if (!(p.hasPermission("carbonchat.formatting") ||
                p.hasPermission("carbonchat.channels." + event.getChannel().getKey() + ".formatting"))) {
            suppressFormatting(event);
            return;
        } else {
            // Swap the &-style codes for minimessage-compatible strings
            event.setMessage(MiniMessage.get().serialize(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(event.getMessage())
                    ));
        }
    }

    private void suppressFormatting(ChatFormatEvent event) {
        event.setFormat(event.getFormat().replace("<message>", "<pre><message></pre>")
        );
    }

}
