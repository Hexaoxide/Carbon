package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatComponentEvent extends Cancellable.Impl implements CarbonEvent {

  private final @NonNull PlayerUser sender;
  private final @Nullable CarbonUser recipient;
  private @NonNull ChatChannel chatChannel;
  private @NonNull TextComponent component;
  private final @NonNull String originalMessage;

  public ChatComponentEvent(final @NonNull PlayerUser sender, final @Nullable CarbonUser recipient,
                            final @NonNull ChatChannel chatChannel, final @NonNull TextComponent component,
                            final @NonNull String originalMessage) {

    this.sender = sender;
    this.recipient = recipient;
    this.chatChannel = chatChannel;
    this.component = component;
    this.originalMessage = originalMessage;
  }

  public @NonNull PlayerUser sender() {
    return this.sender;
  }

  public @Nullable CarbonUser recipient() {
    return this.recipient;
  }

  public @NonNull ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(final @NonNull ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  public @NonNull TextComponent component() {
    return this.component;
  }

  public void component(final @NonNull TextComponent component) {
    this.component = component;
  }

  public @NonNull String originalMessage() {
    return this.originalMessage;
  }

}
