package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;

public class LegacyFormatHandler {

  public LegacyFormatHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, new EventSubscriber<PreChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final PreChatFormatEvent event) {
        final Component component = CarbonChat.LEGACY.deserialize(event.format());
        event.format(MiniMessage.get().serialize(component));
      }
    });
  }

}
