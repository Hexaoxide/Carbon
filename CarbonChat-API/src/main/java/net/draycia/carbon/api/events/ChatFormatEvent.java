package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatFormatEvent implements CarbonEvent, Cancellable {

  private boolean isCancelled = false;
  private final @NonNull PlayerUser sender;
  private final @Nullable PlayerUser target;
  private @NonNull ChatChannel chatChannel;
  private @NonNull String format;
  private @NonNull String message;

  public ChatFormatEvent(final @NonNull PlayerUser sender, final @Nullable PlayerUser target,
                         final @NonNull ChatChannel chatChannel, final @NonNull String format,
                         final @NonNull String message) {

    this.sender = sender;
    this.target = target;

    this.chatChannel = chatChannel;
    this.format = format;
    this.message = message;
  }

  @Override
  public boolean cancelled() {
    return this.isCancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.isCancelled = cancelled;
  }

  public @NonNull PlayerUser sender() {
    return this.sender;
  }

  public @Nullable PlayerUser target() {
    return this.target;
  }

  public @NonNull ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(final @NonNull ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  public @NonNull String format() {
    return this.format;
  }

  public void format(final @NonNull String format) {
    this.format = format;
  }

  public @NonNull String message() {
    return this.message;
  }

  public void message(final @NonNull String message) {
    this.message = message;
  }

}
