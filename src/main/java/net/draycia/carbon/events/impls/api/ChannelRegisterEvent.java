package net.draycia.carbon.events.impls.api;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.util.Registry;
import net.kyori.event.EventBus;
import net.kyori.event.EventSubscriber;
import net.kyori.event.SimpleEventBus;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ChannelRegisterEvent {

  private static EventBus<ChannelRegisterEvent> EVENT_BUS = new SimpleEventBus<>(ChannelRegisterEvent.class);

  public static void register(@NonNull final EventSubscriber<? super ChannelRegisterEvent> subscriber) {
    EVENT_BUS.register(ChannelRegisterEvent.class, subscriber);
  }

  public static void unregister(@NonNull final EventSubscriber<? super ChannelRegisterEvent> subscriber) {
    EVENT_BUS.unregister(subscriber);
  }

  public static void post(@NonNull final ChannelRegisterEvent event) {
    EVENT_BUS.post(event);
  }

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
