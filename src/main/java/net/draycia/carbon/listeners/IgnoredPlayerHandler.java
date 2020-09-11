package net.draycia.carbon.listeners;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;

public class IgnoredPlayerHandler {

  public IgnoredPlayerHandler() {
    CarbonEvents.register(ChatFormatEvent.class, new EventSubscriber<ChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final ChatFormatEvent event) {
        if (event.target() == null) {
          return;
        }

        if (event.target().ignoringUser(event.sender())) {
          event.cancelled(true);
        }
      }
    });
  }

}
