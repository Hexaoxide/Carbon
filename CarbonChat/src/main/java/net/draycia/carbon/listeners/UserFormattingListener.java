package net.draycia.carbon.listeners;

import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UserFormattingListener implements Listener {

    @EventHandler
    public void onFormat(PreChatFormatEvent event) {
        if (!event.getUser().isOnline() || !event.getUser().asPlayer().hasPermission("carbonchat.formatting")) {
            event.setFormat(event.getFormat().replace("<message>", "<pre><message></pre>")
            );
        }
    }

}
