package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatFormatEvent implements CarbonEvent, Cancellable {

  private boolean isCancelled = false;
  @NonNull
  private final ChatUser sender;
  @Nullable
  private final ChatUser target;
  @NonNull
  private ChatChannel chatChannel;
  @Nullable
  private String format;
  @NonNull
  private String message;

  public ChatFormatEvent(final @NonNull ChatUser sender, @Nullable final ChatUser target,
                         final @NonNull ChatChannel chatChannel, @Nullable final String format,
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

  @NonNull
  public ChatUser sender() {
    return this.sender;
  }

  @Nullable
  public ChatUser target() {
    return this.target;
  }

  @NonNull
  public ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(final @NonNull ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  @Nullable
  public String format() {
    return this.format;
  }

  public void format(@Nullable final String format) {
    this.format = format;
  }

  @NonNull
  public String message() {
    return this.message;
  }

  public void message(final @NonNull String message) {
    this.message = message;
  }

}
