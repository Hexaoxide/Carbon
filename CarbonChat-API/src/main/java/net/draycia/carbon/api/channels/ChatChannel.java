package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface ChatChannel extends ForwardingAudience {

  /**
   * Gets a list of {@link ChatUser}s to send channel messages to.
   * @return All {@link ChatUser}s that can see messages in this channel.
   */
  @NonNull List<ChatUser> audiences();

  /**
   * Gets the {@link TextColor} the supplied {@link ChatUser} has set for this channel.
   * If none is set, returns this channel's set color with the "color" config option.
   * @param user The user that may have a color set.
   * @return The color the user may have set, otherwise the channel's color.
   */
  @Nullable TextColor channelColor(@NonNull ChatUser user);

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
   * If this channel can be ignored, such as through the /toggle command.
   * @return IF this channel can be ignored / toggled.
   */
  boolean ignorable();

  /**
   * If this channel syncs between servers, typically through the messaging service.
   * @return If this channel syncs between servers, typically through the messaging service.
   */
  boolean crossServer();

  @NonNull String name();

  @NonNull String key();

  @Nullable String messagePrefix();

  @Nullable List<String> aliases();

  @Nullable String switchMessage();

  @Nullable String switchOtherMessage();

  @Nullable String switchFailureMessage();

  @Nullable String cannotIgnoreMessage();

  @Nullable String toggleOffMessage();

  @Nullable String toggleOnMessage();

  @Nullable String toggleOtherOnMessage();

  @Nullable String toggleOtherOffMessage();

  @Nullable String cannotUseMessage();

  boolean primaryGroupOnly();

  boolean honorsRecipientList();

  boolean permissionGroupMatching();

  boolean shouldCancelChatEvent();

  boolean testContext(@NonNull ChatUser sender, @NonNull ChatUser target);

  @NonNull List<@NonNull Pattern> itemLinkPatterns();

  @Nullable Context context(@NonNull String key);

  @NonNull List<@NonNull String> groupOverrides();

  boolean canPlayerUse(@NonNull ChatUser user);

  boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying);

  boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying);

  @NonNull Map<ChatUser, Component> parseMessage(@NonNull ChatUser user, @NonNull String message, boolean fromBungee);

  @NonNull Map<ChatUser, Component> parseMessage(@NonNull ChatUser user, @NonNull Collection<@NonNull ChatUser> recipients, @NonNull String message, boolean fromBungee);

  void sendComponent(@NonNull ChatUser user, @NonNull Component component);

}
