package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ChannelRegisterEvent implements CarbonEvent {

  @NonNull
  private final List<@NonNull ChatChannel> registeredChannels;
  @NonNull
  private final Registry<String, ChatChannel> registry;

  public ChannelRegisterEvent(@NonNull final List<@NonNull ChatChannel> registeredChannels, @NonNull final Registry<String, ChatChannel> registry) {
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
