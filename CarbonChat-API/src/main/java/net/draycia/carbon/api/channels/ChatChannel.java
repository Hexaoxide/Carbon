package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.CarbonUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
  @Nullable TextColor channelColor(@NonNull CarbonUser user);

  @NonNull
  Map<CarbonUser, Component> parseMessage(@NonNull CarbonUser user, @NonNull String message, boolean fromRemote);

  @NonNull
  Map<CarbonUser, Component> parseMessage(@NonNull CarbonUser user, @NonNull Collection<@NonNull CarbonUser> recipients,
                                          @NonNull String message, boolean fromRemote);

  boolean canPlayerUse(@NonNull CarbonUser user);

  boolean canPlayerSee(@NonNull CarbonUser sender, @NonNull CarbonUser target, boolean checkSpying);

  boolean canPlayerSee(@NonNull CarbonUser target, boolean checkSpying);

  void sendComponents(@NonNull final Map<CarbonUser, Component> components);

  void sendComponentsAndLog(@NonNull final Map<CarbonUser, Component> components);

  @NonNull String name();

  @NonNull String key();

  /**
   * If this channel can be ignored, such as through the /toggle command.
   * @return IF this channel can be ignored / toggled.
   */
  boolean ignorable();

  @NonNull List<@NonNull Pattern> itemLinkPatterns();

  @Nullable String switchMessage();

  @Nullable String switchOtherMessage();

  @Nullable String switchFailureMessage();

  @Nullable String cannotIgnoreMessage();

  @Nullable String toggleOffMessage();

  @Nullable String toggleOnMessage();

  @Nullable String toggleOtherOnMessage();

  @Nullable String toggleOtherOffMessage();

  @Nullable String cannotUseMessage();

}
