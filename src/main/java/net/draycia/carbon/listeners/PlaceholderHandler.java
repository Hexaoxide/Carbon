package net.draycia.carbon.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;

public class PlaceholderHandler {

  public PlaceholderHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, new EventSubscriber<PreChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final PreChatFormatEvent event) {
        event.format(PlaceholderAPI.setPlaceholders(event.user().offlinePlayer(), event.format()));
      }
    });
  }

}
