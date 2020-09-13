package net.draycia.carbon.listeners;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatFormatEvent;
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
