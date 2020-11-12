package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PrivateMessageEvent;
import net.kyori.adventure.sound.Sound;
import net.kyori.event.PostOrders;

public class WhisperPingHandler {

  public WhisperPingHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(PrivateMessageEvent.class, PostOrders.LAST, false, event -> {
      if (event.sender().uuid().equals(event.target().uuid())) {
        return;
      }

      if (!event.message().contains(event.sender().name())) {
        return;
      }

      if (carbonChat.carbonSettings().whisperOptions().pings().enabled()) {
        final Sound sound = event.target().pingOptions().whisperSound();

        if (sound != null) {
          event.target().playSound(sound);
        } else {
          event.target().playSound(carbonChat.carbonSettings().whisperOptions().pings().sound());
        }
      }
    });
  }

}
