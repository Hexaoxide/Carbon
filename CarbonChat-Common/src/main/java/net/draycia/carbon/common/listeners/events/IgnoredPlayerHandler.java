package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;

public class IgnoredPlayerHandler {

  public IgnoredPlayerHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final CarbonUser target = event.target();

      if (!(target instanceof PlayerUser)) {
        return;
      }

      if (((PlayerUser) target).ignoringUser(event.sender())) {
        event.cancelled(true);
      }
    });
  }

}
