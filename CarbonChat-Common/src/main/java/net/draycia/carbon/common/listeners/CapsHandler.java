package net.draycia.carbon.common.listeners;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;

public class CapsHandler {

  public CapsHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!carbonChat.moderationSettings().capsProtection().enabled()) {
        return;
      }

      if (!(event.message().length() >= carbonChat.moderationSettings().capsProtection().minimumLength())) {
        return;
      }

      int amountOfCaps = 0;

      for (final char letter : event.message().toCharArray()) {
        if (Character.isUpperCase(letter)) {
          amountOfCaps++;
        }
      }

      final double capsPercentage = (amountOfCaps * 100.0) / event.message().length();

      if (!(capsPercentage >= carbonChat.moderationSettings().capsProtection().percentCaps())) {
        return;
      }

      if (carbonChat.moderationSettings().capsProtection().blockMessage()) {
        event.cancelled(true);
      } else {
        event.message(event.message().toLowerCase());
      }
    });
  }

}
