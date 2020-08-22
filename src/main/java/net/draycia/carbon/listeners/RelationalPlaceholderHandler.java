package net.draycia.carbon.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RelationalPlaceholderHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPapiPlaceholder(ChatFormatEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        if (!event.getSender().isOnline() || !event.getTarget().isOnline()) {
            return;
        }

        Player sender = event.getSender().asPlayer();
        Player target = event.getTarget().asPlayer();

        event.setFormat(PlaceholderAPI.setRelationalPlaceholders(sender, target, event.getFormat()));
    }

}
