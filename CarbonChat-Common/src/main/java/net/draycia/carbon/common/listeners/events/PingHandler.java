package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.kyori.event.PostOrders;

public class PingHandler {

  public PingHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(ChatComponentEvent.class, PostOrders.LAST, false, event -> {
      if (!carbonChat.carbonSettings().channelPings().enabled()) {
        return;
      }

      if (event.target() == null) {
        return;
      }

      final String targetName = event.target().name();
      final String prefix = carbonChat.carbonSettings().channelPings().prefix();
      final boolean caseSensitive = carbonChat.carbonSettings().channelPings().caseSensitive();

      if (caseSensitive) {
        if (!event.originalMessage().contains(prefix + targetName)) {
          return;
        }
      } else {
        if (!event.originalMessage().toLowerCase().contains((prefix + targetName).toLowerCase())) {
          return;
        }
      }

      event.target().playSound(carbonChat.carbonSettings().channelPings().sound());
    });
  }

}
