package net.draycia.carbon.listeners.events;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PrivateMessageEvent;
import net.draycia.carbon.events.api.PreChatFormatEvent;

public class MuteHandler {

  public MuteHandler() {
    CarbonEvents.register(PrivateMessageEvent.class, event -> {
      if (event.sender().muted()) {
        event.cancelled(true);
      }
    });

    CarbonEvents.register(PreChatFormatEvent.class, event -> {
      if (event.user().muted()) {
        event.cancelled(true);
      }
    });
  }

}
