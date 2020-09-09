package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.PreChatFormatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UserFormattingListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onFormat(PreChatFormatEvent event) {
        if (!event.getUser().isOnline()) {
            suppressFormatting(event);
            return;
        }

        Player player = event.getUser().asPlayer();

        if (!player.hasPermission("carbonchat.formatting") &&
                !player.hasPermission("carbonchat.channels." + event.getChannel().getKey() + ".formatting")) {
            suppressFormatting(event);
        } else {
            // Swap the &-style codes for minimessage-compatible strings
            event.setMessage(MiniMessage.get().serialize(CarbonChat.LEGACY.deserialize(event.getMessage())));
        }
    }

    private void suppressFormatting(PreChatFormatEvent event) {
        event.setFormat(event.getFormat().replace("<message>", "<pre><message></pre>"));
    }

}
