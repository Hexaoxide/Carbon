package net.draycia.carbon.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CarbonPlaceholders extends PlaceholderExpansion {

  @NonNull
  private final CarbonChat carbonChat;

  public CarbonPlaceholders(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @Override
  @NonNull
  public String getIdentifier() {
    return "carbonchat";
  }

  @Override
  @NonNull
  public String getAuthor() {
    return "Draycia (Vicarious#0001)";
  }

  @Override
  @NonNull
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  @Nullable
  public String onPlaceholderRequest(@NonNull final Player player, @NonNull final String identifier) {
    final String key = identifier.toLowerCase();

    if (key.startsWith("can_use_")) {
      final String value = key.replace("can_use_", "");

      final ChatChannel channel = this.carbonChat.channelManager().registry().channel(value);

      if (channel == null) {
        return "false";
      }

      final ChatUser user = this.carbonChat.userService().wrap(player);

      return String.valueOf(channel.canPlayerUse(user));
    } else if (key.startsWith("can_see_")) {
      final String value = key.replace("can_see_", "");

      final ChatChannel channel = this.carbonChat.channelManager().registry().channel(value);

      if (channel == null) {
        return "false";
      }

      final ChatUser user = this.carbonChat.userService().wrap(player);

      return String.valueOf(channel.canPlayerSee(user, true));
    } else if (key.startsWith("ignoring_channel_")) {
      final String value = key.replace("ignoring_channel_", "");

      final ChatChannel channel = this.carbonChat.channelManager().registry().channel(value);

      if (channel == null) {
        return "false";
      }

      final ChatUser user = this.carbonChat.userService().wrap(player);

      return String.valueOf(user.channelSettings(channel).ignored());
    } else if (key.startsWith("selected_channel")) {
      final ChatChannel channel = this.carbonChat.userService().wrap(player).selectedChannel();

      return channel == null ? this.carbonChat.channelManager().defaultChannel().name() : channel.name();
    }

    return null;
  }

}
