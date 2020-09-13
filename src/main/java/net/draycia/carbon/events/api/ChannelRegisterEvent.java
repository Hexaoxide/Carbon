package net.draycia.carbon.events.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.CarbonEvent;
import net.draycia.carbon.util.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ChannelRegisterEvent implements CarbonEvent {

  @NonNull
  private final List<@NonNull ChatChannel> registeredChannels;
  @NonNull
  private final Registry<ChatChannel> registry;

  public ChannelRegisterEvent(@NonNull final List<@NonNull ChatChannel> registeredChannels, @NonNull final Registry<ChatChannel> registry) {
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
