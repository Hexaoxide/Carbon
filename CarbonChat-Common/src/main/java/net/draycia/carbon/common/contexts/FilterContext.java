package net.draycia.carbon.common.contexts;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterContext {

  @NonNull
  private final CarbonChat carbonChat;

  public FilterContext(final @NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!carbonChat.moderationSettings().filters().enabled()) {
        return;
      }

      if (event.user().hasPermission("carbonchat.filter.exempt")) {
        return;
      }

      if (!this.channelUsesFilter(event.channel())) {
        return;
      }

      String message = event.message();

      for (final Map.Entry<String, List<Pattern>> entry :
        this.carbonChat.moderationSettings().filters().replacements().entrySet()) {

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

      for (final Pattern blockedWord : this.carbonChat.moderationSettings().filters().blockedPatterns()) {
        if (blockedWord.matcher(event.message()).find()) {
          event.cancelled(true);
          break;
        }
      }
    });
  }

  private boolean channelUsesFilter(final @NonNull ChatChannel chatChannel) {
    if (chatChannel instanceof TextChannel) {
      final Context context = ((TextChannel) chatChannel).context("filter");

      if (context == null) {
        return false;
      }

      return context.isBoolean() && context.asBoolean();
    }

    return false;
  }

}
