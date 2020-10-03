package net.draycia.carbon.api.events;

import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.adventure.text.Component;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent implements CarbonEvent, Cancellable {

  private final @NonNull ChatUser sender;
  private final @NonNull ChatUser target;
  private final @NonNull Component senderComponent;
  private final @NonNull Component targetComponent;
  private final @NonNull String message;
  private boolean cancelled = false;

  public PrivateMessageEvent(final @NonNull ChatUser sender, final @NonNull ChatUser target,
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

  @NonNull
  public ChatUser sender() {
    return this.sender;
  }

  @NonNull
  public ChatUser target() {
    return this.target;
  }

  @NonNull
  public Component senderComponent() {
    return this.senderComponent;
  }

  @NonNull
  public Component targetComponent() {
    return this.targetComponent;
  }

  @NonNull
  public String message() {
    return this.message;
  }

}
