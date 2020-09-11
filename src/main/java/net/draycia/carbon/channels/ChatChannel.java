package net.draycia.carbon.channels;

import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ChatChannel implements ForwardingAudience {

  @NonNull
  public abstract List<ChatUser> audiences();

  /**
   * @return The color that represents this channel. Optionally used in formatting.
   */
  @Nullable
  public abstract TextColor channelColor(@NonNull ChatUser user);

  /**
   * @return The MiniMessage styled format for the group in this channel.
   */
  @Nullable
  public abstract String format(@NonNull String group);

  /**
   * @return If this is the default (typically Global) channel players use when they're in no other channel.
   */
  public abstract boolean isDefault();

  /**
   * @return If this channel can be toggled off and if players can ignore player messages in this channel.
   */
  public abstract boolean ignorable();

  /**
   * @return If this channel should be synced cross server
   * @deprecated Use {@link #crossServer()} instead
   */
  @Deprecated
  public abstract boolean bungee();

  /**
   * @return If this channel should be synced cross server
   */
  public abstract boolean crossServer();

  /**
   * @return The name of this channel.
   */
  @NonNull
  public abstract String name();

  @NonNull
  public abstract String key();

  @Nullable
  public abstract String messagePrefix();

  @Nullable
  public abstract String aliases();

  /**
   * @return The message to be sent to the player when switching to this channel.
   */
  @Nullable
  public abstract String switchMessage();

  @Nullable
  public abstract String switchOtherMessage();

  @Nullable
  public abstract String switchFailureMessage();

  @Nullable
  public abstract String cannotIgnoreMessage();

  /**
   * @return The message to be send to the player when toggling this channel off.
   */
  @Nullable
  public abstract String toggleOffMessage();

  /**
   * @return The message to be send to the player when toggling this channel on.
   */
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
  public abstract Object context(@NonNull String key);

  @NonNull
  public abstract List<@NonNull String> groupOverrides();

  /**
   * @return If the player can use this channel.
   */
  public abstract boolean canPlayerUse(@NonNull ChatUser user);

  public abstract boolean canPlayerSee(@NonNull ChatUser sender, @NonNull ChatUser target, boolean checkSpying);

  public abstract boolean canPlayerSee(@NonNull ChatUser target, boolean checkSpying);

  /**
   * Parses the specified message, calls a {@link PreChatFormatEvent}, and sends the message to everyone who can view this channel.
   *
   * @param user    The player who is saying the message.
   * @param message The message to be sent.
   */
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
