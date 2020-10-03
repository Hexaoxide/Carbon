package net.draycia.carbon.api.events;

import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.adventure.text.Component;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent implements CarbonEvent, Cancellable {

  private @NonNull final ChatUser sender;
  private @NonNull final ChatUser target;
  private @NonNull final Component senderComponent;
  private @NonNull final Component targetComponent;
  private @NonNull final String message;
  private boolean cancelled = false;

  public PrivateMessageEvent(@NonNull final ChatUser sender, @NonNull final ChatUser target,
                             @NonNull final Component senderComponent, @NonNull final Component targetComponent,
                             @NonNull final String message) {

    this.sender = sender;
    this.target = target;
    this.senderComponent = senderComponent;
    this.targetComponent = targetComponent;
    this.message = message;
  }

  @Override
  public boolean cancelled() {
    return this.cancelled;
  }

  @Override
  public void cancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  public @NonNull ChatUser sender() {
    return this.sender;
  }

  public @NonNull ChatUser target() {
    return this.target;
  }

  public @NonNull Component senderComponent() {
    return this.senderComponent;
  }

  public @NonNull Component targetComponent() {
    return this.targetComponent;
  }

  public @NonNull String message() {
    return this.message;
  }

}
