package net.draycia.carbon.channels;

import co.aikar.commands.CommandManager;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.commands.AliasedChannelCommand;
import net.draycia.carbon.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChannelRegistry implements Registry<ChatChannel> {

    private final Map<String, ChatChannel> registry = new HashMap<>();
    private final CarbonChat carbonChat;

    public ChannelRegistry(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @Override
    public boolean register(@NotNull String key, @NotNull ChatChannel value) {
        boolean registerSuccessful = registry.putIfAbsent(key, value) == null;

        if (registerSuccessful) {
            CommandManager commandManager = carbonChat.getCommandManager().getCommandManager();

            commandManager.getCommandReplacements().addReplacement("channelName", value.getAliases());
            commandManager.registerCommand(new AliasedChannelCommand(value));

            if (value instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener)value, carbonChat);
            }
        }

        return registerSuccessful;
    }

    @Override
    @NotNull
    public Collection<ChatChannel> values() {
        return registry.values();
    }

    @Override
    @Nullable
    public ChatChannel get(String key) {
        return registry.get(key);
    }

}
