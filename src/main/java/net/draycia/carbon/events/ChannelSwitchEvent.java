package net.draycia.carbon.events;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChannelSwitchEvent extends Event implements Cancellable {

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
  private final ChatChannel channel;

  @NonNull
  private final ChatUser user;

  @Nullable
  private String failureMessage;

  public ChannelSwitchEvent(@NonNull final ChatChannel channel, @NonNull final ChatUser user, @Nullable final String failureMessage) {
    super(!Bukkit.isPrimaryThread());

    this.channel = channel;
    this.user = user;
    this.failureMessage = failureMessage;
  }

  @NonNull
  public ChatUser user() {
    return this.user;
  }

  @NonNull
  public ChatChannel channel() {
    return this.channel;
  }

  @Nullable
  public String failureMessage() {
    return this.failureMessage;
  }

  public void failureMessage(@Nullable final String failureMessage) {
    this.failureMessage = failureMessage;
  }
}
