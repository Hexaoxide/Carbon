package net.draycia.carbon.events.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChannelSwitchEvent implements CarbonEvent, Cancellable {

  @NonNull
  private final ChatChannel channel;
  @NonNull
  private final ChatUser user;
  @Nullable
  private String failureMessage;
  private boolean cancelled;

  public ChannelSwitchEvent(@NonNull final ChatChannel channel, @NonNull final ChatUser user, @Nullable final String failureMessage) {
    this.channel = channel;
    this.user = user;
    this.failureMessage = failureMessage;
  }

  @NonNull
  public ChatUser user() {
    return this.user;
  }

  @NonNull
  public ChatChannel channel() {
    return this.channel;
  }

  @Nullable
  public String failureMessage() {
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
