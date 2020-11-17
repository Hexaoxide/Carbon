package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.sound.Sound;
import net.kyori.event.PostOrders;

public class PingHandler {

  public PingHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(ChatComponentEvent.class, PostOrders.LAST, false, event -> {
      if (!carbonChat.carbonSettings().channelPings().enabled()) {
        return;
      }

      final CarbonUser target = event.target();

      if (!(target instanceof PlayerUser)) {
        return;
      }

      final String targetName = target.name();
      final String prefix = carbonChat.carbonSettings().channelPings().prefix();
      final boolean caseSensitive = carbonChat.carbonSettings().channelPings().caseSensitive();
      final String ping = prefix + targetName;

      if (caseSensitive) {
        if (!event.originalMessage().contains(ping)) {
          return;
        }
      } else {
        if (!event.originalMessage().toLowerCase().contains(ping.toLowerCase())) {
          return;
        }
      }

      final Sound sound = ((PlayerUser) target).pingOptions().pingSound();

      if (sound != null) {
        target.playSound(sound);
      } else {
        target.playSound(carbonChat.carbonSettings().channelPings().sound());
      }
    });

    CarbonEvents.register(ChatFormatEvent.class, PostOrders.LAST, false, event -> {
      if (!carbonChat.carbonSettings().channelPings().enabled()) {
        return;
      }

      final CarbonUser target = event.target();

      if (!(target instanceof PlayerUser)) {
        return;
      }

      final String targetName = target.name();
      final String prefix = carbonChat.carbonSettings().channelPings().prefix();
      final boolean caseSensitive = carbonChat.carbonSettings().channelPings().caseSensitive();
      final String ping = prefix + targetName;

      if (caseSensitive) {
        if (!event.message().contains(ping)) {
          return;
        }
      } else {
        if (!event.message().toLowerCase().contains(ping.toLowerCase())) {
          return;
        }
      }

      event.message(event.message().replace(
        ping,
        carbonChat.carbonSettings().channelPings().display().replace("<ping>", ping).replace("<name>", targetName)
      ));
    });
  }

}
