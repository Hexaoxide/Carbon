package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.Context;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReceiverContextEvent implements CarbonEvent, Cancellable {

  private final @NonNull TextChannel channel;
  private final @NonNull ChatUser sender;
  private final @NonNull ChatUser recipient;
  private boolean cancelled = false;

  public ReceiverContextEvent(final @NonNull TextChannel channel, final @NonNull ChatUser sender, final @NonNull ChatUser recipient) {
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

  public @NonNull ChatUser sender() {
    return this.sender;
  }

  public @NonNull ChatUser recipient() {
    return this.recipient;
  }

}
