package net.draycia.carbon.channels;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.commands.AliasedChannelCommand;
import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.CarbonChat;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelRegistry implements Registry<ChatChannel> {

  @NonNull
  private final Map<@NonNull String, @NonNull ChatChannel> registry = new HashMap<>();

  @NonNull
  private final List<@NonNull AliasedChannelCommand> channelCommands = new ArrayList<>();

  @NonNull
  private final CarbonChat carbonChat;

  public ChannelRegistry(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
  }

  @Override
  public boolean register(@NonNull final String key, @NonNull final ChatChannel value) {
    final boolean registerSuccessful = this.registry.putIfAbsent(key, value) == null;

    if (registerSuccessful) {
      final AliasedChannelCommand command = new AliasedChannelCommand(this.carbonChat, value);

      this.channelCommands.add(command);

      if (value instanceof Listener) {
        Bukkit.getPluginManager().registerEvents((Listener) value, this.carbonChat);
      }
    }

    return registerSuccessful;
  }

  @Override
  @NonNull
  public Collection<@NonNull ChatChannel> values() {
    return this.registry.values();
  }

  @Override
  @Nullable
  public ChatChannel channel(@NonNull final String key) {
    return this.registry.get(key);
  }

  @Override
  public void clearAll() {
    this.registry.clear();

    for (final AliasedChannelCommand command : this.channelCommands) {
      this.carbonChat.getLogger().info("Unregistering command for channel: " + command.chatChannel().name());
      CommandAPI.unregister(command.commandName());
    }
  }

}
