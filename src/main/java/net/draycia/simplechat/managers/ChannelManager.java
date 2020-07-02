package net.draycia.simplechat.managers;

import co.aikar.commands.CommandManager;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.*;
import net.draycia.simplechat.commands.AliasedChannelCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class ChannelManager {

    public ChannelManager(SimpleChat simpleChat) {
        boolean hasTownChat = false;
        boolean hasNationChat = false;
        boolean hasAllianceChat = false;
        boolean hasPartyChat = false;

        for (String key : simpleChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
            ChatChannel.Builder builder;

            ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);
            ConfigurationSection defaults = simpleChat.getConfig().getConfigurationSection("default");

            if (section.contains("is-town-chat")) {
                builder = TownChatChannel.townBuilder(key);
                hasTownChat = true;
            } else if (section.contains("is-nation-chat")) {
                builder = NationChatChannel.nationBuilder(key);
                hasNationChat = true;
            } else if (section.contains("is-alliance-chat")) {
                builder = AllianceChatChannel.allianceBuilder(key);
                hasAllianceChat = true;
            } else if (section.contains("is-party-chat")) {
                builder = PartyChatChannel.partyBuilder(key);
                hasPartyChat = true;
            } else {
                builder = SimpleChatChannel.builder(key);
            }

            if (section.contains("id")) {
                builder.setId(section.getLong("id"));
            }

            if (section.contains("formats")) {
                HashMap<String, String> formats = new HashMap<>();

                ConfigurationSection formatSection = section.getConfigurationSection("formats");

                for (String group : formatSection.getKeys(false)) {
                    formats.put(group, formatSection.getString(group));
                }

                builder.setFormats(formats);
            } else if (defaults != null && defaults.contains("formats")) {
                HashMap<String, String> formats = new HashMap<>();

                ConfigurationSection formatSection = defaults.getConfigurationSection("formats");

                for (String group : formatSection.getKeys(false)) {
                    formats.put(group, formatSection.getString(group));
                }

                builder.setFormats(formats);
            }

            if (section.contains("webhook")) {
                builder.setWebhook(section.getString("webhook"));
            }

            if (section.contains("switch-message")) {
                builder.setSwitchMessage(section.getString("switch-message"));
            } else if (defaults != null && defaults.contains("switch-message")) {
                builder.setSwitchMessage(defaults.getString("switch-message"));
            }

            if (section.contains("distance")) {
                builder.setDistance(section.getDouble("distance"));
            } else if (defaults != null && defaults.contains("distance")) {
                builder.setDistance(defaults.getDouble("distance"));
            }

            if (section.contains("name")) {
                builder.setName(section.getString("name"));
            }

            if (section.contains("color")) {
                builder.setColor(section.getString("color"));
            } else if (defaults != null && defaults.contains("color")) {
                builder.setColor(defaults.getString("color"));
            }

            if (section.contains("ignorable")) {
                builder.setIgnorable(section.getBoolean("ignorable"));
            } else if (defaults != null && defaults.contains("ignorable")) {
                builder.setIgnorable(defaults.getBoolean("ignorable"));
            }

            if (section.contains("default")) {
                builder.setIsDefault(section.getBoolean("default"));
            }

            if (section.contains("toggle-on-message")) {
                builder.setToggleOnMessage(section.getString("toggle-on-message"));
            } else if (defaults != null && defaults.contains("toggle-on-message")) {
                builder.setToggleOnMessage(defaults.getString("toggle-on-message"));
            }

            if (section.contains("toggle-off-message")) {
                builder.setToggleOffMessage(section.getString("toggle-off-message"));
            } else if (defaults != null && defaults.contains("toggle-off-message")) {
                builder.setToggleOffMessage(defaults.getString("toggle-off-message"));
            }

            if (section.contains("forward-format")) {
                builder.setShouldForwardFormatting(section.getBoolean("forward-format"));
            } else if (defaults != null && defaults.contains("forward-format")) {
                builder.setShouldForwardFormatting(defaults.getBoolean("forward-format"));
            }

            if (section.contains("should-bungee")) {
                builder.setShouldBungee(section.getBoolean("should-bungee"));
            } else if (defaults != null && defaults.contains("should-bungee")) {
                builder.setShouldBungee(defaults.getBoolean("should-bungee"));
            }

            if (section.contains("filter-enabled")) {
                builder.setFilterEnabled(section.getBoolean("filter-enabled"));
            } else if (defaults != null && defaults.contains("filter-enabled")) {
                builder.setFilterEnabled(defaults.getBoolean("filter-enabled"));
            }

            if (section.contains("first-matching-group")) {
                builder.setFirstMatchingGroup(section.getBoolean("first-matching-group"));
            } else if (defaults != null && defaults.contains("first-matching-group")) {
                builder.setFirstMatchingGroup(defaults.getBoolean("first-matching-group"));
            }

            ChatChannel channel = builder.build(simpleChat);

            if (channel.isTownChat() || channel.isNationChat() || channel.isAllianceChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) {
                    simpleChat.getLogger().warning("Towny related channel with name [" + channel.getName() + "] found, but Towny isn't installed. Skipping channel.");
                    continue;
                }
            }

            if (channel.isPartyChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                    simpleChat.getLogger().warning("mcMMO related channel with name [" + channel.getName() + "] found, but mcMMO isn't installed. Skipping channel.");
                    continue;
                }
            }

            // TODO: register command for each channel
            simpleChat.getChannels().add(channel);

            CommandManager commandManager = simpleChat.getCommandManager().getCommandManager();

            String commandName = section.contains("aliases") ? section.getString("aliases") : channel.getName();

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

}
