package net.draycia.carbon.events.api;

import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.event.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent implements CarbonEvent, Cancellable {

  @NonNull
  private final ChatUser sender;
  @NonNull
  private final ChatUser target;
  @NonNull
  private final Component senderComponent;
  @NonNull
  private final Component targetComponent;
  @NonNull
  private final String message;
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
