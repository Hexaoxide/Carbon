package net.draycia.carbon.events.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PreChatFormatEvent implements CarbonEvent, Cancellable {

  private boolean isCancelled = false;
  @NonNull
  private final ChatUser user;
  @NonNull
  private ChatChannel chatChannel;
  @NonNull
  private String format;
  @NonNull
  private String message;

  public PreChatFormatEvent(@NonNull final ChatUser user, @NonNull final ChatChannel chatChannel,
                            @NonNull final String format, @NonNull final String message) {

    this.user = user;
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
  public ChatUser user() {
    return this.user;
  }

  @NonNull
  public ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(@NonNull final ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  @NonNull
  public String format() {
    return this.format;
  }

  public void format(@NonNull final String format) {
    this.format = format;
  }

  @NonNull
  public String message() {
    return this.message;
  }

  public void message(@NonNull final String message) {
    this.message = message;
  }

}
