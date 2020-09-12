package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CapsHandler {

  public CapsHandler(@NonNull final CarbonChat carbonChat) {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!carbonChat.moderationConfig().getBoolean("caps-protection.enabled")) {
        return;
      }

      if (!(event.message().length() >= carbonChat.moderationConfig().getInt("caps-protection.minimum-length"))) {
        return;
      }

      int amountOfCaps = 0;

      for (final char letter : event.message().toCharArray()) {
        if (Character.isUpperCase(letter)) {
          amountOfCaps++;
        }
      }

      final double capsPercentage = (amountOfCaps * 100.0) / event.message().length();

      if (!(capsPercentage >= carbonChat.moderationConfig().getDouble("caps-protection.percent-caps"))) {
        return;
      }

      if (carbonChat.moderationConfig().getBoolean("block-message")) {
        event.cancelled(true);
      } else {
        event.message(event.message().toLowerCase());
      }
    });
  }

}
