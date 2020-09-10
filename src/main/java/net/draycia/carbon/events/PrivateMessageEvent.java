package net.draycia.carbon.events;

import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent extends Event implements Cancellable {

  /** Bukkit event stuff */
  @NonNull private static final HandlerList handlers = new HandlerList();

  private boolean cancelled = false;

  @Override
  @NonNull
  public HandlerList getHandlers() {
    return handlers;
  }

  @NonNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  /** Relevant stuff */
  @NonNull private final ChatUser sender;

  @NonNull private final ChatUser target;

  @NonNull private final Component senderComponent;

  @NonNull private final Component targetComponent;

  @NonNull private final String message;

  public PrivateMessageEvent(
      @NonNull ChatUser sender,
      @NonNull ChatUser target,
      @NonNull Component senderComponent,
      @NonNull Component targetComponent,
      @NonNull String message) {
    super(!Bukkit.isPrimaryThread());

    this.sender = sender;
    this.target = target;
    this.senderComponent = senderComponent;
    this.targetComponent = targetComponent;
    this.message = message;
  }

  @NonNull
  public ChatUser getSender() {
    return sender;
  }

  @NonNull
  public ChatUser getTarget() {
    return target;
  }

  @NonNull
  public Component getSenderComponent() {
    return senderComponent;
  }

  @NonNull
  public Component getTargetComponent() {
    return targetComponent;
  }

  @NonNull
  public String getMessage() {
    return message;
  }
}
