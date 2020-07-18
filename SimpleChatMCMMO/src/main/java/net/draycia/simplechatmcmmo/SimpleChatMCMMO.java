package net.draycia.simplechatmcmmo;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleChatMCMMO extends JavaPlugin {

    private SimpleChat simpleChat;

    @Override
    public void onEnable() {
        simpleChat = (SimpleChat) Bukkit.getPluginManager().getPlugin("SimpleChat");

        for (String key : getConfig().getConfigurationSection("channels").getKeys(false)) {
            ConfigurationSection section = getConfig().getConfigurationSection("channels").getConfigurationSection(key);

            ChatChannel channel = loadChannel(key, section);

            if (channel != null) {
                if (simpleChat.getChannelManager().registerChannel(channel)) {
                    getLogger().info("Registering channel: " + channel.getName());
                }
            }
        }
    }

    public ChatChannel loadChannel(String key, ConfigurationSection section) {
        ChatChannel channel = new PartyChatChannel(key, simpleChat, section);

        String name = section.getString("name");

        if (name != null && name.length() > 16) {
            getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
            getLogger().warning("Skipping channel, please check your settings!");
            return null;
        }

        return channel;
    }

}
