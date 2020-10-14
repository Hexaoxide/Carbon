package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.config.ChannelOptions;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChannelRegisterEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Map;

public class ChannelManager {

  private @NonNull final CarbonChat carbonChat;

  private @NonNull final ChannelRegistry registry;

  @Nullable
  @MonotonicNonNull
  private String defaultChannelKey = null;

  public ChannelManager(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.registry = new ChannelRegistry();

    this.carbonChat.logger().info("Loading channels!");
    this.reload();
  }

  public @Nullable ChatChannel loadChannel(@NonNull final ChannelOptions settings) {
    final ChatChannel channel = new CarbonChatChannel(this.carbonChat, settings);

    final String name = settings.name();

    if (name.length() > 16) {
      this.carbonChat.logger().error("Channel name [" + name + "] too long! Max length: 16.");
      this.carbonChat.logger().error("Skipping channel, please check your settings!");
      return null;
    }

    return channel;
  }

  public void registerChannel(@NonNull final ChatChannel channel) {
    this.registry().register(channel.key(), channel);

    if (channel instanceof TextChannel) {
      if (((TextChannel) channel).isDefault() && this.defaultChannelKey == null) {
        this.carbonChat.logger().info("Default channel registered: " + channel.name());
        this.defaultChannelKey = channel.key();
      }
    }

    CarbonEvents.post(new ChannelRegisterEvent(Collections.singletonList(channel), this.registry()));
  }

  public @NonNull ChannelRegistry registry() {
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
    this.carbonChat.logger().info("Channels found: " +
      this.carbonChat.channelSettings().channelOptions().size());

    for (final Map.Entry<String, ChannelOptions> options :
      this.carbonChat.channelSettings().channelOptions().entrySet()) {

      final ChatChannel channel = this.loadChannel(options.getValue());

      if (channel != null) {
        this.registerChannel(channel);
        // TODO: register AliasedChannelCommand for each channel
        String prefix = null;

        if (channel instanceof TextChannel) {
          prefix = ((TextChannel) channel).messagePrefix();
        }

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
