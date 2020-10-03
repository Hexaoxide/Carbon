package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatComponentEvent implements CarbonEvent, Cancellable {

  private boolean isCancelled = false;
  private @NonNull final ChatUser sender;
  private @NonNull final ChatUser target;
  private @NonNull ChatChannel chatChannel;
  private @NonNull TextComponent component;
  private @NonNull final String originalMessage;

  public ChatComponentEvent(@NonNull final ChatUser sender, @Nullable final ChatUser target,
                            @NonNull final ChatChannel chatChannel, @NonNull final TextComponent component,
                            @NonNull final String originalMessage) {

    this.sender = sender;
    this.target = target;
    this.chatChannel = chatChannel;
    this.component = component;
    this.originalMessage = originalMessage;
  }

  @Override
  public boolean cancelled() {
    return this.isCancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.isCancelled = cancelled;
  }

  public @NonNull ChatUser sender() {
    return this.sender;
  }

  public @Nullable ChatUser target() {
    return this.target;
  }

  public @NonNull ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(@NonNull final ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  public @NonNull TextComponent component() {
    return this.component;
  }

  public void component(@NonNull final TextComponent component) {
    this.component = component;
  }

  public @NonNull String originalMessage() {
    return this.originalMessage;
  }

}
