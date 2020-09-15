package net.draycia.carbon.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;

public class OfflineNameHandler {

  public OfflineNameHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.LATE, false, event -> {
      // If the player isn't online (cross server message), use their normal name
      if (Bukkit.getOfflinePlayer(event.user().uuid()) == null) {
        event.format(event.format().replace("%player_displayname%", "%player_name%"));
      }
    });
  }

}
