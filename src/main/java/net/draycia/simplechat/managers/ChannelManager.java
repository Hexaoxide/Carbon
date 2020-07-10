package net.draycia.simplechat.managers;

import co.aikar.commands.CommandManager;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.*;
import net.draycia.simplechat.commands.AliasedChannelCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class ChannelManager {

    public ChannelManager(SimpleChat simpleChat) {
        boolean hasTownChat = false;
        boolean hasNationChat = false;
        boolean hasAllianceChat = false;
        boolean hasPartyChat = false;

        for (String key : simpleChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
            ChatChannel channel;

            ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

            String name = section.getString("name");

            if (name != null && name.length() > 16) {
                simpleChat.getLogger().warning("Channel name [" + name + "] too long! Max length: 16.");
                simpleChat.getLogger().warning("Skipping channel, please check your settings!");
                continue;
            }

            String type = section.getString("type");

            if (type == null) {
                type = "normal";
            }

            switch (type.toLowerCase()) {
                case "towny":
                    channel = new TownChatChannel(key, simpleChat);
                    hasTownChat = true;
                    break;
                case "nation":
                    channel = new NationChatChannel(key, simpleChat);
                    hasNationChat = true;
                    break;
                case "alliance":
                    channel = new AllianceChatChannel(key, simpleChat);
                    hasAllianceChat = true;
                    break;
                case "party":
                    channel = new PartyChatChannel(key, simpleChat);
                    hasPartyChat = true;
                    break;
                case "normal":
                    channel = new SimpleChatChannel(key, simpleChat);
                    break;
                default:
                    simpleChat.getLogger().warning("Invalid channel type for channel [" + key + "]. Skipping channel.");
                    continue;
            }

            if (channel.isTownChat() || channel.isNationChat() || channel.isAllianceChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) {
                    simpleChat.getLogger().warning("Towny related channel with key [" + key + "] found, but Towny isn't installed. Skipping channel.");
                    continue;
                }
            }

            if (channel.isPartyChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                    simpleChat.getLogger().warning("mcMMO related channel with key [" + key + "] found, but mcMMO isn't installed. Skipping channel.");
                    continue;
                }
            }

            simpleChat.getChannels().add(channel);

            CommandManager commandManager = simpleChat.getCommandManager().getCommandManager();

            String commandName = section.contains("aliases") ? section.getString("aliases") : key;

            commandManager.getCommandReplacements().addReplacement("channelName", commandName);
            commandManager.registerCommand(new AliasedChannelCommand(simpleChat, channel));
        }

        boolean townyInstalled = Bukkit.getPluginManager().isPluginEnabled("Towny");
        boolean mcmmoInstalled = Bukkit.getPluginManager().isPluginEnabled("mcMMO");

        if (!hasTownChat && townyInstalled) {
            simpleChat.getLogger().info("Towny installed but no Town channel is setup!");
        }

        if (!hasNationChat && townyInstalled) {
            simpleChat.getLogger().info("Towny installed but no Nation channel is setup!");
        }

        if (!hasAllianceChat && townyInstalled) {
            simpleChat.getLogger().info("Towny installed but no Alliance channel is setup!");
        }

        if (!hasPartyChat && mcmmoInstalled) {
            simpleChat.getLogger().info("mcMMO installed but no Party channel is setup!");
        }
    }

    public void reloadChannels() {

    }

}
