package net.draycia.carbon.events.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.util.Context;
import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MessageContextEvent implements CarbonEvent, Cancellable {

  private @NonNull final ChatChannel channel;
  private @NonNull final ChatUser user;
  private boolean cancelled = false;

  public MessageContextEvent(final @NonNull ChatChannel channel, final @NonNull ChatUser user) {
    this.channel = channel;
    this.user = user;
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  public Context context(@NonNull final String key) {
    return this.channel.context(key);
  }

  public @NonNull ChatChannel channel() {
    return this.channel;
  }

  public @NonNull ChatUser user() {
    return this.user;
  }

}
