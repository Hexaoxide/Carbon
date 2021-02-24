package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface TextChannel extends ChatChannel, ForwardingAudience {

  @Override
  @NonNull List<@NonNull PlayerUser> audiences();

  /**
   * Gets the string format for the specified group.
   * @param group The group.
   * @return The string format for the specified group.
   */
  @Nullable String format(@NonNull Group group);

  /**
   * Gets the string format for the specified group.
   * @param group The group.
   * @return The string format for the specified group.
   */
  @Nullable String format(@NonNull String group);

  /**
   * If this channel is the channel players get when they join for the first time.
   * Also the fallback channel in case the code is unable to find any given channel.
   * @return If this is the default channel.
   */
  boolean isDefault();

  /**
   * If this channel syncs between servers, typically through the messaging service.
   * @return If this channel syncs between servers, typically through the messaging service.
   */
  boolean crossServer();

  /**
   * The prefix used to quickly send messages in this channel without switching to it
   * Or, null if none is set.
   */
  @Nullable String messagePrefix();

  /**
   * List of command aliases
   */
  @NonNull List<String> aliases();

  /**
   * If the format chooser should only consider the player's primary permission group
   */
  boolean primaryGroupOnly();

  /**
   * If having the permission carbonchat.group.groupname counts as having said permission group
   */
  boolean permissionGroupMatching();

  /**
   * Gets the context with the associated key
   */
  @Nullable Context context(@NonNull String key);

  /**
   * The custom group priority list for determining which chat format to use
   */
  @NonNull List<@NonNull String> groupOverrides();

  /**
   * Sends the given component to everyone in this channel
   * @param sender The sender of the component
   * @param component The component to be sent
   */
  void sendComponent(@NonNull PlayerUser sender, @NonNull Component component);

}
