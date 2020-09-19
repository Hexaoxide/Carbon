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

      if (!carbonChat.getConfig().getBoolean("whisper.pings.enabled")) {
        return;
      }

      final Key key = Key.of(carbonChat.getConfig().getString("whisper.pings.sound"));
      final Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("whisper.pings.source"));
      final float volume = (float) carbonChat.getConfig().getDouble("whisper.pings.volume");
      final float pitch = (float) carbonChat.getConfig().getDouble("whisper.pings.pitch");

      event.sender().playSound(Sound.of(key, source, volume, pitch));
    });
  }

}
