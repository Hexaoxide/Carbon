package net.draycia.carbon.events.impls;

import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PrivateMessageEvent extends Event implements Cancellable {

  /**
   * Bukkit event stuff
   */
  @NonNull
  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;

  @Override
  @NonNull
  public HandlerList getHandlers() {
    return handlers;
  }

  @NonNull
  @SuppressWarnings("checkstyle:MethodName")
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  /**
   * Relevant stuff
   */
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

  public PrivateMessageEvent(@NonNull final ChatUser sender, @NonNull final ChatUser target,
                             @NonNull final Component senderComponent, @NonNull final Component targetComponent,
                             @NonNull final String message) {

    super(!Bukkit.isPrimaryThread());

    this.sender = sender;
    this.target = target;
    this.senderComponent = senderComponent;
    this.targetComponent = targetComponent;
    this.message = message;
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
