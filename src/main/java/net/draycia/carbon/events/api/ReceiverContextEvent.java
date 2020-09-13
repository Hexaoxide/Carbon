package net.draycia.carbon.events.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.util.Context;
import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReceiverContextEvent implements CarbonEvent, Cancellable {

  private @NonNull final ChatChannel channel;
  private @NonNull final ChatUser sender;
  private @NonNull final ChatUser recipient;
  private boolean cancelled = false;

  public ReceiverContextEvent(final @NonNull ChatChannel channel, final @NonNull ChatUser sender, final @NonNull ChatUser recipient) {
    this.channel = channel;
    this.sender = sender;
    this.recipient = recipient;
  }

  public Context context(@NonNull final String key) {
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

  public @NonNull ChatChannel channel() {
    return this.channel;
  }

  public @NonNull ChatUser sender() {
    return this.sender;
  }

  public @NonNull ChatUser recipient() {
    return this.recipient;
  }
}
