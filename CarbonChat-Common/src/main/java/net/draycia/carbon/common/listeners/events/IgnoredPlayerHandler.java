package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.kyori.event.PostOrders;

public class IgnoredPlayerHandler {

  public IgnoredPlayerHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (event.target() == null) {
        return;
      }

      if (event.target().ignoringUser(event.sender())) {
        event.cancelled(true);
      }
    });
  }

}
