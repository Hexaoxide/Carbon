package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface WhisperChannel extends ChatChannel, ForwardingAudience.Single {

  @NonNull ChatUser sender();

  /**
   * The recipient for this channel, where messages are sent to.
   * @return The channel recipient.
   */
  @Override
  @NonNull ChatUser audience();

  @Override
  default @NonNull String name() {
    return "Party";
  }

  @Override
  default @NonNull String key() {
    return "party";
  }

}
