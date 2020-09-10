package net.draycia.carbon.events;

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

  public ChannelRegisterEvent(@NonNull List<@NonNull ChatChannel> registeredChannels, @NonNull Registry<ChatChannel> registry) {
    super(!Bukkit.isPrimaryThread());

    this.registeredChannels = registeredChannels;
    this.registry = registry;
  }

  public void register(@NonNull ChatChannel chatChannel) {
    registry.register(chatChannel.getKey(), chatChannel);
  }

  @NonNull
  public List<@NonNull ChatChannel> getRegisteredChannels() {
    return registeredChannels;
  }
}
