package net.draycia.carbon.api.events;

import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.Component;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent extends Cancellable.Impl implements CarbonEvent {

  private final @NonNull PlayerUser sender;
  private final @NonNull PlayerUser recipient;
  private final @NonNull Component senderComponent;
  private final @NonNull Component recipientComponent;
  private final @NonNull String message;

  public PrivateMessageEvent(final @NonNull PlayerUser sender, final @NonNull PlayerUser recipient,
                             final @NonNull Component senderComponent, final @NonNull Component recipientComponent,
                             final @NonNull String message) {

    this.sender = sender;
    this.recipient = recipient;
    this.senderComponent = senderComponent;
    this.recipientComponent = recipientComponent;
    this.message = message;
  }

  public @NonNull PlayerUser sender() {
    return this.sender;
  }

  public @NonNull PlayerUser recipient() {
    return this.recipient;
  }

  public @NonNull Component senderComponent() {
    return this.senderComponent;
  }

  public @NonNull Component recipientComponent() {
    return this.recipientComponent;
  }

  public @NonNull String message() {
    return this.message;
  }

}
