package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.kyori.event.PostOrders;

public class ShadowMuteHandler {

  public ShadowMuteHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(ChatComponentEvent.class, PostOrders.FIRST, false, event -> {
      if (event.sender().shadowMuted() && event.sender().equals(event.target())) {
        event.cancelled(true);
      }
    });

    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (event.target() != null) {
        return;
      }

      if (!event.sender().shadowMuted()) {
        return;
      }

      final String prefix = carbonChat.moderationSettings().shadowMutePrefix();

      event.format(prefix + event.format());
    });
  }

}
