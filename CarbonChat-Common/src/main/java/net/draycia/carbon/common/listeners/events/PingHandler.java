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
      if (!carbonChat.carbonSettings().channelPings().enabled()) {
        return;
      }

      if (event.target() == null) {
        return;
      }

      final String targetName = event.target().name();
      final String prefix = carbonChat.carbonSettings().channelPings().prefix();
      final boolean caseSensitive = carbonChat.carbonSettings().channelPings().caseSensitive();

      if (caseSensitive) {
        if (!event.originalMessage().contains(prefix + targetName)) {
          return;
        }
      } else {
        if (!event.originalMessage().toLowerCase().contains((prefix + targetName).toLowerCase())) {
          return;
        }
      }

      final Key key = carbonChat.carbonSettings().channelPings().sound();
      final Sound.Source source = carbonChat.carbonSettings().channelPings().source();
      final float volume = carbonChat.carbonSettings().channelPings().volume();
      final float pitch = carbonChat.carbonSettings().channelPings().pitch();

      event.target().playSound(Sound.sound(key, source, volume, pitch));
    });
  }

}
