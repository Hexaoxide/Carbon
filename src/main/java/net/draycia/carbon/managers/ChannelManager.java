package net.draycia.carbon.managers;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChannelRegistry;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.impls.CarbonChatChannel;
import net.draycia.carbon.events.impls.ChannelRegisterEvent;
import net.draycia.carbon.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

  public ChannelManager(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.registry = new ChannelRegistry(carbonChat);

    this.reload();
  }

  public @Nullable ChatChannel loadChannel(@NonNull final String key, @NonNull final ConfigurationSection section) {
    final ChatChannel channel = new CarbonChatChannel(key, this.carbonChat, section);

    final String name = section.getString("name");

    if (name != null && name.length() > 16) {
      this.carbonChat.getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
      this.carbonChat.getLogger().warning("Skipping channel, please check your settings!");
      return null;
    }

    return channel;
  }

  public boolean registerChannel(@NonNull final ChatChannel channel) {
    final boolean success = this.registry().register(channel.key(), channel);

    if (success) {
      if (channel.isDefault() && this.defaultChannelKey == null) {
        this.carbonChat.getLogger().info("Default channel registered: " + channel.name());
        this.defaultChannelKey = channel.key();
      }

      Bukkit.getPluginManager().callEvent(new ChannelRegisterEvent(Collections.singletonList(channel), this.registry()));
    }

    return success;
  }

  public @NonNull Registry<ChatChannel> registry() {
    return this.registry;
  }

  public @Nullable ChatChannel defaultChannel() {
    if (this.defaultChannelKey != null) {
      return this.registry.channel(this.defaultChannelKey);
    }

    return null;
  }

  public @Nullable ChatChannel channelOrDefault(@Nullable final String key) {
    if (key == null) {
      return this.defaultChannel();
    }

    final ChatChannel channel = this.registry.channel(key);

    if (channel == null) {
      return this.defaultChannel();
    }

    return channel;
  }

  private void reload() {
    this.registry.clearAll();

    for (final String key : this.carbonChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
      final ConfigurationSection section =
        this.carbonChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

      final ChatChannel channel = this.loadChannel(key, section);

      if (channel != null) {
        if (this.registerChannel(channel)) {
          String prefix = channel.messagePrefix();

          if (prefix != null && !prefix.trim().isEmpty()) {
            prefix = "(" + prefix + ") ";
          } else {
            prefix = "";
          }

          this.carbonChat.getLogger().info("Registering channel: " + prefix + channel.name());
        }
      }
    }
  }

}
