package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;

public class IgnoredPlayerHandler {

  public IgnoredPlayerHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final PlayerUser target = event.target();

      if (target == null) {
        return;
      }

      if (target.ignoringUser(event.sender())) {
        event.cancelled(true);
      }
    });
  }

}
