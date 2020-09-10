package net.draycia.carbon.events.impls;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PreChatFormatEvent extends Event implements Cancellable {

  @NonNull
  private static final HandlerList HANDLERS_LIST = new HandlerList();
  private boolean isCancelled = false;

  @NonNull
  private final ChatUser user;

  @NonNull
  private ChatChannel chatChannel;

  @Nullable
  private String format;

  @NonNull
  private String message;

  public PreChatFormatEvent(@NonNull final ChatUser user, @NonNull final ChatChannel chatChannel,
                            @Nullable final String format, @NonNull final String message) {
    super(!Bukkit.isPrimaryThread());

    this.user = user;
    this.chatChannel = chatChannel;
    this.format = format;
    this.message = message;
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
  public ChatUser user() {
    return this.user;
  }

  @NonNull
  public ChatChannel channel() {
    return this.chatChannel;
  }

  public void channel(@NonNull final ChatChannel chatChannel) {
    this.chatChannel = chatChannel;
  }

  @Nullable
  public String format() {
    return this.format;
  }

  public void format(@Nullable final String format) {
    this.format = format;
  }

  @NonNull
  public String message() {
    return this.message;
  }

  public void message(@NonNull final String message) {
    this.message = message;
  }

}
