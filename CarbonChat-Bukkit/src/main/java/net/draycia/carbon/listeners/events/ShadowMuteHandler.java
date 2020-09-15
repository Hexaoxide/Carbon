package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteHandler {

  public ShadowMuteHandler(@NonNull final CarbonChat carbonChat) {
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

      final String prefix = carbonChat.moderationConfig().getString("shadow-mute-prefix", "[SM] ");

      event.format(prefix + event.format());
    });
  }

}
