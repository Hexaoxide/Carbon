package net.draycia.carbon.listeners.events;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.event.PostOrders;

public class LegacyFormatHandler {

  public LegacyFormatHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      event.format(CarbonUtils.translateAlternateColors(event.format()));
    });
  }

}
