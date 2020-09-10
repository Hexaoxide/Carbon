package net.draycia.carbon.events;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ChatComponentEvent extends Event implements Cancellable {

  @NonNull
  private static final HandlerList HANDLERS_LIST = new HandlerList();
  private boolean isCancelled = false;

  @NonNull
  private final ChatUser sender;

  @Nullable
  private final ChatUser target;

  @NonNull
  private ChatChannel chatChannel;

  @NonNull
  private TextComponent component;

  @NonNull
  private final String originalMessage;

  public ChatComponentEvent(@NonNull final ChatUser sender, @Nullable final ChatUser target,
                            @NonNull final ChatChannel chatChannel, @NonNull final TextComponent component,
                            @NonNull final String originalMessage) {

    super(!Bukkit.isPrimaryThread());

    this.sender = sender;
    this.target = target;
    this.chatChannel = chatChannel;
    this.component = component;
    this.originalMessage = originalMessage;
  }

  @Override
  public boolean isCancelled() {
    return this.isCancelled;
  }

  @Override
  public void setCancelled(final boolean cancelled) {
    this.isCancelled = cancelled;
  }

  @Override
  @NonNull
  public HandlerList getHandlers() {
    return HANDLERS_LIST;
  }

  @NonNull
  @SuppressWarnings("checkstyle:MethodName")
  public static HandlerList getHandlerList() {
    return HANDLERS_LIST;
  }

  @NonNull
  public ChatUser sender() {
    return this.sender;
  }

  @Nullable
  public ChatUser target() {
    return this.target;
  }

  @NonNull
  public ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(@NonNull final ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  @NonNull
  public TextComponent component() {
    return this.component;
  }

  public void component(@NonNull final TextComponent component) {
    this.component = component;
  }

  @NonNull
  public String originalMessage() {
    return this.originalMessage;
  }

}
