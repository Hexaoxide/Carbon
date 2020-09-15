package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ChatChannel implements ForwardingAudience {

  /**
   * Gets a list of {@link ChatUser}s to send channel messages to.
   * @return All {@link ChatUser}s that can see messages in this channel.
   */
  @NonNull
  public abstract List<ChatUser> audiences();

  /**
   * Gets the {@link TextColor} the supplied {@link ChatUser} has set for this channel.
   * If none is set, returns this channel's set color with the "color" config option.
   * @param user The user that may have a color set.
   * @return The color the user may have set, otherwise the channel's color.
   */
  @Nullable
  public abstract TextColor channelColor(@NonNull ChatUser user);

  /**
   * Gets the string format for the specified group.
   * @param group The group.
   * @return The string format for the specified group.
   */
  @Nullable
  public abstract String format(@NonNull String group);

  /**
   * If this channel is the channel players get when they join for the first time.
   * Also the fallback channel in case the code is unable to find any given channel.
   * @return If this is the default channel.
   */
  public abstract boolean isDefault();

  /**
   * If this channel can be ignored, such as through the /toggle command.
   * @return IF this channel can be ignored / toggled.
   */
  public abstract boolean ignorable();

  /**
   * If this channel syncs between servers, typically through the messaging service.
   * @return If this channel syncs between servers, typically through the messaging service.
   */
  public abstract boolean crossServer();

  @NonNull
  public abstract String name();

  @NonNull
  public abstract String key();

  @Nullable
  public abstract String messagePrefix();

  @Nullable
  public abstract String aliases();

  @Nullable
  public abstract String switchMessage();

  @Nullable
  public abstract String switchOtherMessage();

  @Nullable
  public abstract String switchFailureMessage();

  @Nullable
  public abstract String cannotIgnoreMessage();

  @Nullable
  public abstract String toggleOffMessage();

  @Nullable
  public abstract String toggleOnMessage();

  @Nullable
  public abstract String toggleOtherOnMessage();

  @Nullable
  public abstract String toggleOtherOffMessage();

  @Nullable
  public abstract String cannotUseMessage();

  public abstract boolean primaryGroupOnly();

  public abstract boolean honorsRecipientList();

  public abstract boolean permissionGroupMatching();

  public abstract boolean shouldCancelChatEvent();

  public abstract boolean testContext(@NonNull ChatUser sender, @NonNull ChatUser target);

  @NonNull
  public abstract List<@NonNull Pattern> itemLinkPatterns();

  @Nullable
  public abstract Context context(@NonNull String key);

  @NonNull
  public abstract List<@NonNull String> groupOverrides();

  public abstract boolean canPlayerUse(@NonNull ChatUser user);

  public abstract boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying);

  public abstract boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying);

  @NonNull
  public abstract Component sendMessage(@NonNull ChatUser user, @NonNull String message, boolean fromBungee);

  @NonNull
  public abstract Component sendMessage(@NonNull ChatUser user, @NonNull Collection<@NonNull ChatUser> recipients, @NonNull String message, boolean fromBungee);

  public abstract void sendComponent(@NonNull ChatUser user, @NonNull Component component);

  @Nullable
  public String processPlaceholders(@NonNull final ChatUser user, @Nullable final String input) {
    return input;
  }

}
