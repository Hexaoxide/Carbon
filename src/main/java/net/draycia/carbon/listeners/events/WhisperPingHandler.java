package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PrivateMessageEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WhisperPingHandler {

  public WhisperPingHandler(@NonNull final CarbonChat carbonChat) {

    CarbonEvents.register(PrivateMessageEvent.class, PostOrders.LAST, false, event -> {
      if (event.sender().uuid().equals(event.target().uuid())) {
        return;
      }

      final String senderName = event.sender().offlinePlayer().getName();

      if (senderName == null || !event.message().contains(senderName)) {
        return;
      }

      if (!carbonChat.getConfig().getBoolean("whisper.pings.enabled")) {
        return;
      }

      final Key key = Key.key(carbonChat.getConfig().getString("whisper.pings.sound"));
      final Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("whisper.pings.source"));
      final float volume = (float) carbonChat.getConfig().getDouble("whisper.pings.volume");
      final float pitch = (float) carbonChat.getConfig().getDouble("whisper.pings.pitch");

      event.sender().playSound(Sound.sound(key, source, volume, pitch));
    });
  }

}
