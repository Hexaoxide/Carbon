package net.draycia.carbon.listeners;

import net.draycia.carbon.events.PreChatFormatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OfflineNameHandler implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onOfflineMessage(final PreChatFormatEvent event) {
    // If the player isn't online (cross server message), use their normal name
    if (!event.user().online()) {
      event.format(event.format().replace("%player_displayname%", "%player_name%"));
    }
  }

}
