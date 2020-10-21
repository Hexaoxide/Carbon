package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReceiverContextEvent implements CarbonEvent, Cancellable {

  private final @NonNull TextChannel channel;
  private final @NonNull PlayerUser sender;
  private final @NonNull PlayerUser recipient;
  private boolean cancelled = false;

  public ReceiverContextEvent(final @NonNull TextChannel channel, final @NonNull PlayerUser sender,
                              final @NonNull PlayerUser recipient) {
    this.channel = channel;
    this.sender = sender;
    this.recipient = recipient;
  }

  public Context context(final @NonNull String key) {
    return this.channel.context(key);
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  public @NonNull TextChannel channel() {
    return this.channel;
  }

  public @NonNull PlayerUser sender() {
    return this.sender;
  }

  public @NonNull PlayerUser recipient() {
    return this.recipient;
  }

}
