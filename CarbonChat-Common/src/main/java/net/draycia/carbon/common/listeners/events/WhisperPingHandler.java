package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PrivateMessageEvent;
import net.kyori.adventure.key.Key;
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

      if (!carbonChat.carbonSettings().whisperOptions().pings().enabled()) {
        return;
      }

      final Key key = carbonChat.carbonSettings().whisperOptions().pings().sound();
      final Sound.Source source = carbonChat.carbonSettings().whisperOptions().pings().source();
      final float volume = carbonChat.carbonSettings().whisperOptions().pings().volume();
      final float pitch = carbonChat.carbonSettings().whisperOptions().pings().pitch();

      event.sender().playSound(Sound.of(key, source, volume, pitch));
    });
  }

}
