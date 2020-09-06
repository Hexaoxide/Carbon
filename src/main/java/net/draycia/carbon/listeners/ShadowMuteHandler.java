package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteHandler implements Listener {

    private final CarbonChat carbonChat;

    public ShadowMuteHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onComponent(@NonNull ChatComponentEvent event) {
        if (event.getSender().isShadowMuted()) {
            if (!event.getSender().equals(event.getTarget())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(@NonNull ChatFormatEvent event) {
        if (event.getTarget() != null) {
            return;
        }

        if (!event.getSender().isShadowMuted()) {
            return;
        }

        String prefix = carbonChat.getModConfig().getString("shadow-mute-prefix", "[SM] ");

        event.setFormat(prefix + event.getFormat());
    }

}
