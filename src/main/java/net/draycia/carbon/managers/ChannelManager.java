package net.draycia.carbon.managers;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChannelRegistry;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.impls.CarbonChatChannel;
import net.draycia.carbon.events.ChannelRegisterEvent;
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

  public ChannelManager(CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.registry = new ChannelRegistry(carbonChat);

    reload();
  }

  public @Nullable ChatChannel loadChannel(String key, @NonNull ConfigurationSection section) {
    ChatChannel channel = new CarbonChatChannel(key, carbonChat, section);

    String name = section.getString("name");

    if (name != null && name.length() > 16) {
      carbonChat.getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
      carbonChat.getLogger().warning("Skipping channel, please check your settings!");
      return null;
    }

    return channel;
  }

  public boolean registerChannel(@NonNull ChatChannel channel) {
    boolean success = getRegistry().register(channel.getKey(), channel);

    if (success) {
      if (channel.isDefault() && defaultChannelKey == null) {
        carbonChat.getLogger().info("Default channel registered: " + channel.getName());
        defaultChannelKey = channel.getKey();
      }

      Bukkit.getPluginManager().callEvent(new ChannelRegisterEvent(Collections.singletonList(channel), getRegistry()));
    }

    return success;
  }

  public @NonNull Registry<ChatChannel> getRegistry() {
    return registry;
  }

  public @Nullable ChatChannel getDefaultChannel() {
    if (defaultChannelKey != null) {
      return registry.get(defaultChannelKey);
    }

    return null;
  }

  public @Nullable ChatChannel getChannelOrDefault(@Nullable String key) {
    if (key == null) {
      return getDefaultChannel();
    }

    ChatChannel channel = registry.get(key);

    if (channel == null) {
      return getDefaultChannel();
    }

    return channel;
  }

  private void reload() {
    registry.clearAll();

    for (String key : carbonChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
      ConfigurationSection section = carbonChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

      ChatChannel channel = loadChannel(key, section);

      if (channel != null) {
        if (registerChannel(channel)) {
          String prefix = channel.getMessagePrefix();

          if (prefix != null && !prefix.trim().isEmpty()) {
            prefix = ("(" + prefix + ") ");
          } else {
            prefix = "";
          }

          carbonChat.getLogger().info("Registering channel: " + prefix + channel.getName());
        }
      }
    }
  }

}
