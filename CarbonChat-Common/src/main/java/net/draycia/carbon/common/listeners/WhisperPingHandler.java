package net.draycia.carbon.common.listeners;

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
      if (event.sender().uuid().equals(event.recipient().uuid())) {
        return;
      }

      if (!event.message().contains(event.sender().username())) {
        return;
      }

      if (carbonChat.carbonSettings().whisperOptions().pings().enabled()) {
        final Sound sound = event.recipient().pingOptions().whisperSound();

        if (sound != null) {
          event.recipient().playSound(sound);
        } else {
          event.recipient().playSound(carbonChat.carbonSettings().whisperOptions().pings().sound());
        }
      }
    });
  }

}
