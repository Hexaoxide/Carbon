package net.draycia.carbon.listeners;

import net.draycia.carbon.events.ChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class IgnoredPlayerHandler implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPapiPlaceholder(final ChatFormatEvent event) {
    if (event.target() == null) {
      return;
    }

    if (event.target().ignoringUser(event.sender())) {
      event.setCancelled(true);
    }
  }

}
