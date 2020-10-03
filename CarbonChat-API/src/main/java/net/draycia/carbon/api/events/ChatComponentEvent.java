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
  private final @NonNull ChatUser sender;
  @Nullable
  private final ChatUser target;
  @NonNull
  private ChatChannel chatChannel;
  @NonNull
  private TextComponent component;
  private final @NonNull String originalMessage;

  public ChatComponentEvent(final @NonNull ChatUser sender, final @Nullable ChatUser target,
                            final @NonNull ChatChannel chatChannel, final @NonNull TextComponent component,
                            final @NonNull String originalMessage) {

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

  @NonNull
  public ChatUser sender() {
    return this.sender;
  }

  public @Nullable ChatUser target() {
    return this.target;
  }

  @NonNull
  public ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(final @NonNull ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  @NonNull
  public TextComponent component() {
    return this.component;
  }

  public void component(final @NonNull TextComponent component) {
    this.component = component;
  }

  @NonNull
  public String originalMessage() {
    return this.originalMessage;
  }

}
