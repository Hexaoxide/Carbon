package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PrivateMessageEvent;
import net.draycia.carbon.api.events.PreChatFormatEvent;

public class MuteHandler {

  public MuteHandler() {
    CarbonEvents.register(PrivateMessageEvent.class, event -> {
      if (event.sender().muted()) {
        event.cancelled(true);
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      if (event.sender().muted()) {
        event.cancelled(true);
      }
    });
  }

}
