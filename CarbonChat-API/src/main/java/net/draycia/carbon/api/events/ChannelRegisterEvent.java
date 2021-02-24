package net.draycia.carbon.api.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvent;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelRegisterEvent implements CarbonEvent {

  private final @NonNull ChatChannel channel;
  private final @NonNull Registry<String, ChatChannel> registry;

  public ChannelRegisterEvent(final @NonNull ChatChannel channel, final @NonNull Registry<String, ChatChannel> registry) {
    this.channel = channel;
    this.registry = registry;
  }

  public void register(final @NonNull ChatChannel chatChannel) {
    this.registry.register(chatChannel.key(), chatChannel);
  }

  public @NonNull ChatChannel channel() {
    return this.channel;
  }

}
