package net.draycia.carbon.common.listeners;

import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.common.utils.ColorUtils;
import net.kyori.event.PostOrders;

public class LegacyFormatHandler {

  public LegacyFormatHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      event.format(ColorUtils.translateAlternateColors(event.format()));
    });
  }

}
