package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChannelSwitchEvent implements CarbonEvent, Cancellable {

  private final @NonNull ChatChannel channel;
  private final @NonNull ChatUser user;
  @Nullable
  private String failureMessage;
  private boolean cancelled = false;

  public ChannelSwitchEvent(final @NonNull ChatChannel channel, final @NonNull ChatUser user, final @Nullable String failureMessage) {
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

  public @Nullable String failureMessage() {
    return this.failureMessage;
  }

  public void failureMessage(final @Nullable String failureMessage) {
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
