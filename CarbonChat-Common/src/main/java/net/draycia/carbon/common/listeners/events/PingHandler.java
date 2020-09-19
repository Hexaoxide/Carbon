package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.event.PostOrders;

public class PingHandler {

  public PingHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(ChatComponentEvent.class, PostOrders.LAST, false, event -> {
      if (!carbonChat.getConfig().getBoolean("pings.enabled")) {
        return;
      }

      if (event.target() == null) {
        return;
      }

      final String targetName = event.target().name();
      final String prefix = carbonChat.getConfig().getString("pings.prefix", "");
      final boolean caseSensitive = carbonChat.getConfig().getBoolean("pings.case-sensitive", false);

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
