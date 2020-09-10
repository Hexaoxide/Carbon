package net.draycia.carbon.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CarbonPlaceholders extends PlaceholderExpansion {

  @NonNull private final CarbonChat carbonChat;

  public CarbonPlaceholders(@NonNull CarbonChat carbonChat) {
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
  public String onPlaceholderRequest(
      @NonNull final Player player, @NonNull final String identifier) {
    String key = identifier.toLowerCase();

    if (key.startsWith("can_use_")) {
      String value = key.replace("can_use_", "");

      ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      ChatUser user = carbonChat.getUserService().wrap(player);

      return String.valueOf(channel.canPlayerUse(user));
    } else if (key.startsWith("can_see_")) {
      String value = key.replace("can_see_", "");

      ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      ChatUser user = carbonChat.getUserService().wrap(player);

      return String.valueOf(channel.canPlayerSee(user, true));
    } else if (key.startsWith("ignoring_channel_")) {
      String value = key.replace("ignoring_channel_", "");

      ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(value);

      if (channel == null) {
        return "false";
      }

      ChatUser user = carbonChat.getUserService().wrap(player);

      return String.valueOf(user.getChannelSettings(channel).isIgnored());
    } else if (key.startsWith("selected_channel")) {
      ChatChannel channel = carbonChat.getUserService().wrap(player).getSelectedChannel();

      return channel == null
          ? carbonChat.getChannelManager().getDefaultChannel().getName()
          : channel.getName();
    }

    return null;
  }
}
