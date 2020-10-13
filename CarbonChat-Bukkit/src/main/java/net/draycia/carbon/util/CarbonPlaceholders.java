package net.draycia.carbon.util;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.CarbonChatBukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CarbonPlaceholders extends PlaceholderExpansion {

  private @NonNull final CarbonChatBukkit carbonChat;

  public CarbonPlaceholders(@NonNull final CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
  }

  @Override
  public @NonNull String getIdentifier() {
    return "carbonchat";
  }

  @Override
  public @NonNull String getAuthor() {
    return "Draycia (Vicarious#0001)";
  }

  @Override
  public @NonNull String getVersion() {
    return "1.0.0";
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public @Nullable String onPlaceholderRequest(@NonNull final Player player, @NonNull final String identifier) {
    final String key = identifier.toLowerCase();

    if (key.startsWith("can_use_")) {
      final String value = key.replace("can_use_", "");

      final ChatChannel channel = this.carbonChat.channelRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      final CarbonUser user = this.carbonChat.userService().wrap(player.getUniqueId());

      return String.valueOf(channel.canPlayerUse(user));
    } else if (key.startsWith("can_see_")) {
      final String value = key.replace("can_see_", "");

      final ChatChannel channel = this.carbonChat.channelRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      final CarbonUser user = this.carbonChat.userService().wrap(player.getUniqueId());

      return String.valueOf(channel.canPlayerSee(user, true));
    } else if (key.startsWith("ignoring_channel_")) {
      final String value = key.replace("ignoring_channel_", "");

      final ChatChannel channel = this.carbonChat.channelRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      final CarbonUser user = this.carbonChat.userService().wrap(player.getUniqueId());

      return String.valueOf(user.channelSettings(channel).ignored());
    } else if (key.startsWith("selected_channel")) {
      final ChatChannel channel = this.carbonChat.userService().wrap(player.getUniqueId()).selectedChannel();

      return channel == null ? this.carbonChat.channelRegistry().defaultChannel().name() : channel.name();
    }

    return null;
  }

}
