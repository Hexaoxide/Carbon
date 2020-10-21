package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;

import java.util.Map;

public class CustomPlaceholderHandler {

  public CustomPlaceholderHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      for (final Map.Entry<String, String> entry : carbonChat.channelSettings().customPlaceholders().entrySet()) {
        event.format(event.format().replace("<" + entry.getKey() + ">", entry.getValue()));
      }
    });
  }

}
