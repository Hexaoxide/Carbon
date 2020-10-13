package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

// TODO: rename this to something that's less... weird?
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

  @Nullable String messagePrefix();

  @Nullable List<String> aliases();

  boolean primaryGroupOnly();

  boolean honorsRecipientList();

  boolean permissionGroupMatching();

  boolean testContext(@NonNull PlayerUser sender, @NonNull PlayerUser target);

  @Nullable Context context(@NonNull String key);

  @NonNull List<@NonNull String> groupOverrides();

  void sendComponent(@NonNull PlayerUser user, @NonNull Component component);

}
