package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatComponentEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PingHandler {

  public PingHandler(@NonNull final CarbonChat carbonChat) {
    CarbonEvents.register(ChatComponentEvent.class, PostOrders.LAST, false, event -> {
      if (!carbonChat.getConfig().getBoolean("pings.enabled")) {
        return;
      }

      if (event.target() == null) {
        return;
      }

      final String targetName = event.target().offlinePlayer().getName();
      final String prefix = carbonChat.getConfig().getString("pings.prefix", "");
      final boolean caseSensitive = carbonChat.getConfig().getBoolean("pings.case-sensitive", false);

      if (targetName == null) {
        return;
      }

      if (caseSensitive) {
        if (!event.originalMessage().contains(prefix + targetName)) {
          return;
        }
      } else {
        if (!event.originalMessage().toLowerCase().contains((prefix + targetName).toLowerCase())) {
          return;
        }
      }

      final Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
      final Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
      final float volume = (float) carbonChat.getConfig().getDouble("pings.volume");
      final float pitch = (float) carbonChat.getConfig().getDouble("pings.pitch");

      event.target().playSound(Sound.of(key, source, volume, pitch));
    });
  }

}
