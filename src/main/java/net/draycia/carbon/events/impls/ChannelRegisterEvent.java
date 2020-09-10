package net.draycia.carbon.events.impls;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ChannelRegisterEvent extends Event {

  /**
   * Bukkit event stuff
   */
  @NonNull
  private static final HandlerList handlers = new HandlerList();

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

  /**
   * Relevant stuff
   */
  @NonNull
  private final List<@NonNull ChatChannel> registeredChannels;

  @NonNull
  private final Registry<ChatChannel> registry;

  public ChannelRegisterEvent(@NonNull final List<@NonNull ChatChannel> registeredChannels, @NonNull final Registry<ChatChannel> registry) {
    super(!Bukkit.isPrimaryThread());

    this.registeredChannels = registeredChannels;
    this.registry = registry;
  }

  public void register(@NonNull final ChatChannel chatChannel) {
    this.registry.register(chatChannel.key(), chatChannel);
  }

  @NonNull
  public List<@NonNull ChatChannel> registeredChannels() {
    return this.registeredChannels;
  }
}
