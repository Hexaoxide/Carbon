package net.draycia.carbon.api.events;

import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.Component;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent implements CarbonEvent, Cancellable {

  private final @NonNull PlayerUser sender;
  private final @NonNull PlayerUser target;
  private final @NonNull Component senderComponent;
  private final @NonNull Component targetComponent;
  private final @NonNull String message;
  private boolean cancelled = false;

  public PrivateMessageEvent(final @NonNull PlayerUser sender, final @NonNull PlayerUser target,
                             final @NonNull Component senderComponent, final @NonNull Component targetComponent,
                             final @NonNull String message) {

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

  public @NonNull PlayerUser sender() {
    return this.sender;
  }

  public @NonNull PlayerUser target() {
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
