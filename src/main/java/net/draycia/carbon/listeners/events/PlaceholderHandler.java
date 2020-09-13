package net.draycia.carbon.listeners.events;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.event.PostOrders;

public class PlaceholderHandler {

  public PlaceholderHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      event.format(PlaceholderAPI.setPlaceholders(event.user().offlinePlayer(), event.format()));
    });
  }

}
