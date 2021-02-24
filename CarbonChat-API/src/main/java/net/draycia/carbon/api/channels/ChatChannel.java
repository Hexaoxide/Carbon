package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.config.ChannelOptions;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface ChatChannel extends Audience {

  /**
   * Gets the {@link TextColor} the supplied {@link CarbonUser} has set for this channel.
   * If none is set, returns this channel's set color with the "color" config option.
   * @param user The user that may have a color set.
   * @return The color the user may have set, otherwise the channel's color.
   */
  @NonNull TextColor channelColor(@NonNull CarbonUser user);

  /**
   * Creates a map of User -> Component from a sender and message, where each component is sent to the corresponding user
   * @param sender The message sender
   * @param message The message the sender sent
   * @param fromRemote If the message originates from a different server
   * @return The user -> component map
   */
  @NonNull
  Map<CarbonUser, Component> parseMessage(@NonNull PlayerUser sender, @NonNull String message, boolean fromRemote);

  /**
   * Creates a map of User -> Component from a sender and message, where each component is sent to the corresponding user
   * @param sender The message sender
   * @param recipients The players that'll receive the message
   * @param message The message the sender sent
   * @param fromRemote If the message originates from a different server
   * @return The user -> component map
   */
  @NonNull
  Map<CarbonUser, Component> parseMessage(@NonNull PlayerUser sender, @NonNull Collection<@NonNull PlayerUser> recipients,
                                          @NonNull String message, boolean fromRemote);

  /**
   * Checks if the player is allowed to speak in the channel
   * @param user The player
   * @return If the player is allowed to speak in the channel
   */
  boolean canPlayerUse(@NonNull PlayerUser user);

  /**
   * Checks if the target can see messages from the sender in this channel
   * @param sender The sender
   * @param target The target
   * @param checkSpying Returns true if the target has spying enabled for the message
   * @return If the target can see messages from the sender in this channel
   */
  boolean canPlayerSee(@NonNull PlayerUser sender, @NonNull PlayerUser target, boolean checkSpying);

  /**
   * Checks if the target can see messages in this channel
   * @param target The target
   * @param checkSpying Returns true if the target has spying enabled for the message
   * @return If the target can see messages in this channel
   */
  boolean canPlayerSee(@NonNull PlayerUser target, boolean checkSpying);

  /**
   * Sends each player their respective component, with the given identity.
   * @param identity The message / component's identity
   * @param components The components to be sent
   */
  void sendComponents(final @NonNull Identity identity,
                      final @NonNull Map<? extends CarbonUser, Component> components);

  /**
   * Sends each player their respective component, with the given identity.
   * Also logs message to console.
   * @param identity The message / component's identity
   * @param components The components to be sent
   */
  void sendComponentsAndLog(final @NonNull Identity identity,
                            final @NonNull Map<? extends CarbonUser, Component> components);

  /**
   * The name of this channel
   */
  @NonNull String name();

  /**
   * The key for this channel, typically used for identification purposes
   */
  @NonNull String key();

  /**
   * If this channel can be ignored, such as through the /toggle command.
   */
  boolean ignorable();

  /**
   * A list of regex patterns used to show items in chat
   */
  @NonNull List<@NonNull Pattern> itemLinkPatterns();

  /**
   * The message that's shown when switching to a channel
   */
  @NonNull String switchMessage();

  /**
   * The message that's shown when switching another player's channels
   */
  @NonNull String switchOtherMessage();

  /**
   * The message that's shown when you cannot switch to a channel
   */
  @NonNull String switchFailureMessage();

  /**
   * The message that's shown when you attempt to ignore a channel but cannot
   */
  @NonNull String cannotIgnoreMessage();

  /**
   * The message that's shown when you hide a channel
   */
  @NonNull String toggleOffMessage();

  /**
   * The message that's shown when you un-hide a channel
   */
  @NonNull String toggleOnMessage();

  /**
   * The message that's shown when you un-hide a channel for someone else
   */
  @NonNull String toggleOtherOnMessage();

  /**
   * The message that's shown when you hide a channel for someone else
   */
  @NonNull String toggleOtherOffMessage();

  /**
   * The message that's shown when you attempt to speak in this channel but are unable to
   */
  @NonNull String cannotUseMessage();

  /**
   * This channel's options
   */
  @NonNull ChannelOptions options();

  /**
   * Sets this channel's options
   * @param options The options to be set
   */
  void options(@NonNull ChannelOptions options);

}
