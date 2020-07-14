package net.draycia.simplechat.managers;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChannelRegistry;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.*;
import net.draycia.simplechat.events.ChannelRegisterEvent;
import net.draycia.simplechat.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {

    private ChannelRegistry registry;
    private String defaultChannelKey = null;

    public ChannelManager(SimpleChat simpleChat) {
        registry = new ChannelRegistry(simpleChat);
        List<ChatChannel> channels = new ArrayList<>();

        for (String key : simpleChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
            ChatChannel channel = new SimpleChatChannel(key, simpleChat);

            ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

            String name = section.getString("name");

            if (name != null && name.length() > 16) {
                simpleChat.getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
                simpleChat.getLogger().warning("Skipping channel, please check your settings!");
                continue;
            }

            channels.add(channel);

            if (channel.isDefault()) {
                simpleChat.getLogger().info("Default channel found: " + channel.getName());
                defaultChannelKey = channel.getKey();
            }

        }

        for (ChatChannel channel : channels) {
            simpleChat.getLogger().info("Registering channel: " + channel.getName());
            getRegistry().register(channel.getKey(), channel);
        }

        Bukkit.getPluginManager().callEvent(new ChannelRegisterEvent(channels, getRegistry()));
    }

    public Registry<ChatChannel> getRegistry() {
        return registry;
    }

    public ChatChannel getDefaultChannel() {
        if (defaultChannelKey != null) {
            return registry.get(defaultChannelKey);
        }

        return null;
    }

    public ChatChannel getChannelOrDefault(String key) {
        if (key == null) {
            return getDefaultChannel();
        }

        ChatChannel channel = registry.get(key);

        if (channel == null) {
            return getDefaultChannel();
        }

        return channel;
    }

}
