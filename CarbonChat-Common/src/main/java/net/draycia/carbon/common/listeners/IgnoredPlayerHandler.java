package net.draycia.carbon.common.listeners;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.PostOrders;

public class IgnoredPlayerHandler {

  public IgnoredPlayerHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final CarbonUser recipient = event.recipient();

      if (!(recipient instanceof PlayerUser)) {
        return;
      }

      if (((PlayerUser) recipient).ignoringUser(event.sender())) {
        event.cancelled(true);
      }
    });
  }

}
