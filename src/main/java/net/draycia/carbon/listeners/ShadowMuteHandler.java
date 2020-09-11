package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatComponentEvent;
import net.draycia.carbon.events.api.ChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteHandler {

  public ShadowMuteHandler(@NonNull final CarbonChat carbonChat) {
    CarbonEvents.register(ChatComponentEvent.class, new EventSubscriber<ChatComponentEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final ChatComponentEvent event) {
        if (event.sender().shadowMuted()) {
          if (!event.sender().equals(event.target())) {
            event.cancelled(true);
          }
        }
      }
    });

    CarbonEvents.register(ChatFormatEvent.class, new EventSubscriber<ChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final ChatFormatEvent event) {
        if (event.target() != null) {
          return;
        }

        if (!event.sender().shadowMuted()) {
          return;
        }

        final String prefix = carbonChat.moderationConfig().getString("shadow-mute-prefix", "[SM] ");

        event.format(prefix + event.format());
      }
    });
  }

}
