package net.draycia.carbon.listeners;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;

public class OfflineNameHandler {

  public OfflineNameHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, new EventSubscriber<PreChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.LATE;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final PreChatFormatEvent event) {
        // If the player isn't online (cross server message), use their normal name
        if (!event.user().online()) {
          event.format(event.format().replace("%player_displayname%", "%player_name%"));
        }
      }
    });
  }

}
