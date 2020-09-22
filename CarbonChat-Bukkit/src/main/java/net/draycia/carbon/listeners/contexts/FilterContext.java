package net.draycia.carbon.listeners.contexts;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.Context;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterContext {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final Map<String, List<@NonNull Pattern>> patternReplacements = new HashMap<>();

  @NonNull
  private final List<@NonNull Pattern> blockedWords = new ArrayList<>();

  public FilterContext(final @NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.reloadFilters();

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      //      if (!carbonChat.moderationSettings().filters().enabled()) {
      //        return;
      //      }

      if (event.user().permissible() && event.user().hasPermission("carbonchat.filter.exempt")) {
        return;
      }

      if (!this.channelUsesFilter(event.channel())) {
        return;
      }

      String message = event.message();

      for (final Map.Entry<String, List<Pattern>> entry : this.patternReplacements.entrySet()) {
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

      for (final Pattern blockedWord : this.blockedWords) {
        if (blockedWord.matcher(event.message()).find()) {
          event.cancelled(true);
          break;
        }
      }
    });
  }

  public void reloadFilters() {
    this.patternReplacements.clear();
    this.blockedWords.clear();

    //    final FileConfiguration config = this.carbonChat.moderationConfig();
    //    final ConfigurationSection filters = config.getConfigurationSection("filters.filters");
    //
    //    if (filters != null) {
    //      for (final String replacement : filters.getKeys(false)) {
    //        final List<Pattern> patterns = new ArrayList<>();
    //
    //        for (final String word : filters.getStringList(replacement)) {
    //          if (this.carbonChat.moderationConfig().getBoolean("filters.case-sensitive")) {
    //            patterns.add(Pattern.compile(word));
    //          } else {
    //            patterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
    //          }
    //        }
    //
    //        this.patternReplacements.put(replacement, patterns);
    //      }
    //    }
    //
    //    for (final String replacement : config.getStringList("filters.blocked-words")) {
    //      if (this.carbonChat.moderationConfig().getBoolean("filters.case-sensitive")) {
    //        this.blockedWords.add(Pattern.compile(replacement));
    //      } else {
    //        this.blockedWords.add(Pattern.compile(replacement, Pattern.CASE_INSENSITIVE));
    //      }
    //    }
  }

  private boolean channelUsesFilter(final @NonNull ChatChannel chatChannel) {
    final Context context = chatChannel.context("filter");

    if (context == null) {
      return false;
    }

    return context.isBoolean() && context.asBoolean();
  }

}
