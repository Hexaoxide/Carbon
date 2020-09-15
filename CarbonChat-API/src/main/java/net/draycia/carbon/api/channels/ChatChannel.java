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

  @NonNull
  public abstract List<ChatUser> audiences();

  @Nullable
  public abstract TextColor channelColor(@NonNull ChatUser user);

  @Nullable
  public abstract String format(@NonNull String group);

  public abstract boolean isDefault();

  public abstract boolean ignorable();

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
