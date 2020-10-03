package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ChannelRegisterEvent implements CarbonEvent {

  private final @NonNull List<@NonNull ChatChannel> registeredChannels;
  private final @NonNull Registry<String, ChatChannel> registry;

  public ChannelRegisterEvent(final @NonNull List<@NonNull ChatChannel> registeredChannels, final @NonNull Registry<String, ChatChannel> registry) {
    this.registeredChannels = registeredChannels;
    this.registry = registry;
  }

  public void register(final @NonNull ChatChannel chatChannel) {
    this.registry.register(chatChannel.key(), chatChannel);
  }

  @NonNull
  public List<@NonNull ChatChannel> registeredChannels() {
    return this.registeredChannels;
  }

}
