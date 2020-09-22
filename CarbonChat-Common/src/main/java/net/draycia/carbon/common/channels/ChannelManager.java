package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChannelRegisterEvent;
import net.kyori.registry.Registry;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

public class ChannelManager {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final ChannelRegistry registry;

  @Nullable
  @MonotonicNonNull
  private String defaultChannelKey = null;

  public ChannelManager(final @NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.registry = new ChannelRegistry();

    this.reload();
  }

  public @Nullable ChatChannel loadChannel(final @NonNull ChannelSettings settings) {
    final ChatChannel channel = new CarbonChatChannel(this.carbonChat, settings);

    final String name = settings.name();

    if (name.length() > 16) {
      this.carbonChat.logger().error("Channel name [" + name + "] too long! Max length: 16.");
      this.carbonChat.logger().error("Skipping channel, please check your settings!");
      return null;
    }

    return channel;
  }

  public void registerChannel(final @NonNull ChatChannel channel) {
    this.registry().register(channel.key(), channel);

    if (channel.isDefault() && this.defaultChannelKey == null) {
      this.carbonChat.logger().info("Default channel registered: " + channel.name());
      this.defaultChannelKey = channel.key();
    }

    CarbonEvents.post(new ChannelRegisterEvent(Collections.singletonList(channel), this.registry()));
  }

  public @NonNull Registry<String, ChatChannel> registry() {
    return this.registry;
  }

  public @Nullable ChatChannel defaultChannel() {
    if (this.defaultChannelKey != null) {
      return this.registry.get(this.defaultChannelKey);
    }

    return null;
  }

  public @Nullable ChatChannel channelOrDefault(@Nullable final String key) {
    if (key == null) {
      return this.defaultChannel();
    }

    final ChatChannel channel = this.registry.get(key);

    if (channel == null) {
      return this.defaultChannel();
    }

    return channel;
  }

  private void reload() {
    for (final ChannelSettings settings : this.carbonChat.carbonSettings().channelSettings()) {
      final ChatChannel channel = this.loadChannel(settings);

      if (channel != null) {
        this.registerChannel(channel);
        String prefix = channel.messagePrefix();

        if (prefix != null && !prefix.trim().isEmpty()) {
          prefix = "(" + prefix + ") ";
        } else {
          prefix = "";
        }

        this.carbonChat.logger().info("Registering channel: " + prefix + channel.name());
      }
    }
  }

}
