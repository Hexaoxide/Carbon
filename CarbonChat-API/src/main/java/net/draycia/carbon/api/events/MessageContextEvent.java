package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MessageContextEvent implements CarbonEvent, Cancellable {

  private final @NonNull TextChannel channel;
  private final @NonNull PlayerUser user;
  private boolean cancelled = false;

  public MessageContextEvent(final @NonNull TextChannel channel, final @NonNull PlayerUser user) {
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

  public Context context(final @NonNull String key) {
    return this.channel.context(key);
  }

  public @NonNull TextChannel channel() {
    return this.channel;
  }

  public @NonNull PlayerUser user() {
    return this.user;
  }

}
