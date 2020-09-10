package net.draycia.carbon.events;

import net.draycia.carbon.events.impls.ChatFormatEvent;
import net.kyori.event.SimpleEventBus;

public class Events {

  public Events() {
    final SimpleEventBus<ChatFormatEvent> bus = new SimpleEventBus<>(ChatFormatEvent.class);

    bus.register(ChatFormatEvent.class, event -> {

    });
  }

}
