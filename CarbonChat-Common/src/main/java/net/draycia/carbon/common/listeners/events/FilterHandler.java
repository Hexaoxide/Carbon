package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterHandler {

  @SuppressWarnings("method.invocation.invalid")
  public FilterHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!carbonChat.moderationSettings().filters().enabled()) {
        return;
      }

      if (event.sender().hasPermission("carbonchat.filter.exempt") ||
        event.sender().hasPermission("carbonchat.filter.exempt." + event.channel().key())) {
        return;
      }

      String message = event.message();

      for (final Map.Entry<String, List<Pattern>> entry :
        carbonChat.moderationSettings().filters().replacements().entrySet()) {

        for (final Pattern pattern : entry.getValue()) {
          final Matcher matcher = pattern.matcher(message);

          if (entry.getKey().equals("_")) {
            message = matcher.replaceAll("");
          } else {
            message = matcher.replaceAll(entry.getKey());
          }
        }
      }

      event.message(message);

      for (final Pattern blockedWord : carbonChat.moderationSettings().filters().blockedPatterns()) {
        if (blockedWord.matcher(event.message()).find()) {
          event.cancelled(true);
          break;
        }
      }
    });
  }

}
