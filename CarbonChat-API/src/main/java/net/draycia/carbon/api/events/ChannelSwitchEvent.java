package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChannelSwitchEvent implements CarbonEvent, Cancellable {

  private @NonNull final ChatChannel channel;
  private @NonNull final ChatUser user;
  private @NonNull String failureMessage;
  private boolean cancelled = false;

  public ChannelSwitchEvent(@NonNull final ChatChannel channel, @NonNull final ChatUser user, @Nullable final String failureMessage) {
    this.channel = channel;
    this.user = user;
    this.failureMessage = failureMessage;
  }

  public @NonNull ChatUser user() {
    return this.user;
  }

  public @NonNull ChatChannel channel() {
    return this.channel;
  }

  public @Nullable String failureMessage() {
    return this.failureMessage;
  }

  public void failureMessage(@Nullable final String failureMessage) {
    this.failureMessage = failureMessage;
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

}
