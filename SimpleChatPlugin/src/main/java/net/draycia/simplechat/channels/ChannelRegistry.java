package net.draycia.simplechat.channels;

import co.aikar.commands.CommandManager;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.commands.AliasedChannelCommand;
import net.draycia.simplechat.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChannelRegistry implements Registry<ChatChannel> {

    private Map<String, ChatChannel> registry = new HashMap<>();
    private SimpleChat simpleChat;

    public ChannelRegistry(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Override
    public boolean register(@NotNull String key, @NotNull ChatChannel value) {
        boolean registerSuccessful = registry.putIfAbsent(key, value) == null;

        if (registerSuccessful) {
            CommandManager commandManager = simpleChat.getCommandManager().getCommandManager();

            commandManager.getCommandReplacements().addReplacement("channelName", value.getAliases());
            commandManager.registerCommand(new AliasedChannelCommand(simpleChat, value));

            if (value instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener)value, simpleChat);
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
