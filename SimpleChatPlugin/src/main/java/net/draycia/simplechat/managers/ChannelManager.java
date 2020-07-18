package net.draycia.simplechat.managers;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChannelRegistry;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.*;
import net.draycia.simplechat.events.ChannelRegisterEvent;
import net.draycia.simplechat.util.Registry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;

public class ChannelManager {

    private ChannelRegistry registry;
    private String defaultChannelKey = null;
    private SimpleChat simpleChat;

    public ChannelManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
        this.registry = new ChannelRegistry(simpleChat);

        for (String key : simpleChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
            ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

            ChatChannel channel = loadChannel(key, section);

            if (channel != null) {
                if (registerChannel(channel)) {
                    simpleChat.getLogger().info("Registering channel: " + channel.getName());
                }
            }
        }
    }

    public ChatChannel loadChannel(String key, ConfigurationSection section) {
        ChatChannel channel = new SimpleChatChannel(key, simpleChat, section);

        String name = section.getString("name");

        if (name != null && name.length() > 16) {
            simpleChat.getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
            simpleChat.getLogger().warning("Skipping channel, please check your settings!");
            return null;
        }

        return channel;
    }

    public boolean registerChannel(ChatChannel channel) {
         boolean success = getRegistry().register(channel.getKey(), channel);

         if (success) {
             if (channel.isDefault() && defaultChannelKey == null) {
                 simpleChat.getLogger().info("Default channel registered: " + channel.getName());
                 defaultChannelKey = channel.getKey();
             }

             Bukkit.getPluginManager().callEvent(new ChannelRegisterEvent(Collections.singletonList(channel), getRegistry()));
         }

         return success;
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
