package net.draycia.carbon.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlaceholderHandler implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPapiPlaceholder(PreChatFormatEvent event) {
    event.setFormat(PlaceholderAPI.setPlaceholders(event.getUser().offlinePlayer(), event.getFormat()));
  }

}
