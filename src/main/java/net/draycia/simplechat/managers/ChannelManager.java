package net.draycia.simplechat.managers;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class ChannelManager {

    private SimpleChat simpleChat;

    public ChannelManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        boolean hasTownChat = false;
        boolean hasNationChat = false;
        boolean hasAllianceChat = false;
        boolean hasPartyChat = false;

        for (String key : simpleChat.getConfig().getConfigurationSection("channels").getKeys(false)) {
            ChatChannel.Builder builder;

            ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("channels").getConfigurationSection(key);

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
            }

            if (section.contains("webhook")) {
                builder.setWebhook(section.getString("webhook"));
            }

            if (section.contains("switch-message")) {
                builder.setSwitchMessage(section.getString("switch-message"));
            }

            if (section.contains("distance")) {
                builder.setDistance(section.getDouble("distance"));
            }

            if (section.contains("name")) {
                builder.setName(section.getString("name"));
            }

            if (section.contains("color")) {
                builder.setColor(section.getString("color"));
            }

            if (section.contains("ignorable")) {
                builder.setIgnorable(section.getBoolean("ignorable"));
            }

            if (section.contains("default")) {
                builder.setIsDefault(section.getBoolean("default"));
            }

            if (section.contains("toggle-on-message")) {
                builder.setToggleOnMessage(section.getString("toggle-on-message"));
            }

            if (section.contains("toggle-off-message")) {
                builder.setToggleOffMessage(section.getString("toggle-off-message"));
            }

            if (section.contains("forward-format")) {
                builder.setShouldForwardFormatting(section.getBoolean("forward-format"));
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
        }

        if (!hasTownChat) {
            simpleChat.getLogger().info("Towny installed but no Town channel is setup!");
        }

        if (!hasNationChat) {
            simpleChat.getLogger().info("Towny installed but no Nation channel is setup!");
        }

        if (!hasAllianceChat) {
            simpleChat.getLogger().info("Towny installed but no Alliance channel is setup!");
        }

        if (!hasPartyChat) {
            simpleChat.getLogger().info("mcMMO installed but no Party channel is setup!");
        }
    }


}
