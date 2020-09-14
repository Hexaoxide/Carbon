package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.event.PostOrders;

public class LegacyFormatHandler {

  public LegacyFormatHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final Component component = CarbonChat.LEGACY.deserialize(event.format());
      event.format(MiniMessage.get().serialize(component));
    });
  }

}
