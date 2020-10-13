package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatFormatEvent implements CarbonEvent, Cancellable {

  private boolean isCancelled = false;
  private @NonNull final CarbonUser sender;
  private @NonNull final CarbonUser target;
  private @NonNull ChatChannel chatChannel;
  private @NonNull String format;
  private @NonNull String message;

  public ChatFormatEvent(@NonNull final CarbonUser sender, @Nullable final CarbonUser target,
                         @NonNull final ChatChannel chatChannel, @Nullable final String format,
                         @NonNull final String message) {

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

  public @NonNull CarbonUser sender() {
    return this.sender;
  }

  public @Nullable CarbonUser target() {
    return this.target;
  }

  public @NonNull ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(@NonNull final ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  public @Nullable String format() {
    return this.format;
  }

  public void format(@Nullable final String format) {
    this.format = format;
  }

  public @NonNull String message() {
    return this.message;
  }

  public void message(@NonNull final String message) {
    this.message = message;
  }

}
