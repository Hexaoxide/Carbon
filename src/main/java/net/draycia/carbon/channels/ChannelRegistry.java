package net.draycia.carbon.channels;

import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.commands.AliasedChannelCommand;
import net.draycia.carbon.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class ChannelRegistry implements Registry<ChatChannel> {

    private final Map<String, ChatChannel> registry = new HashMap<>();
    private final List<AliasedChannelCommand> channelCommands = new ArrayList<>();

    private final CarbonChat carbonChat;

    public ChannelRegistry(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @Override
    public boolean register(@NonNull String key, @NonNull ChatChannel value) {
        boolean registerSuccessful = registry.putIfAbsent(key, value) == null;

        if (registerSuccessful) {
            AliasedChannelCommand command = new AliasedChannelCommand(carbonChat, value);

            channelCommands.add(command);

            if (value instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener)value, carbonChat);
            }
        }

        return registerSuccessful;
    }

    @Override
    @NonNull
    public Collection<ChatChannel> values() {
        return registry.values();
    }

    @Override
    @Nullable
    public ChatChannel get(@NonNull String key) {
        return registry.get(key);
    }

    @Override
    public void clearAll() {
        registry.clear();

        for (AliasedChannelCommand command : channelCommands) {
            carbonChat.getLogger().info("Unregistering command for channel: " +  command.getChatChannel().getName());
            CommandAPI.unregister(command.getCommandName());
        }
    }

}
