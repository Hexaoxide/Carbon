package net.draycia.simplechat;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.SimpleChatChannel;
import net.draycia.simplechat.commands.ChannelCommand;
import net.draycia.simplechat.commands.ToggleCommand;
import net.draycia.simplechat.listeners.PlayerChatListener;
import net.kyori.text.format.TextColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();

    private HashMap<UUID, ChatChannel> userChannels = new HashMap<>(); // TODO: persistence
    private HashMap<UUID, ArrayList<String>> userToggles = new HashMap<>(); // TODO: persistence
    private HashMap<UUID, ArrayList<UUID>> userIgnores = new HashMap<>(); // TODO: persistence

    private Permission permission;

    private DiscordApi discordAPI = null;

    @Override
    public void onEnable() {
        permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();

        saveDefaultConfig();

        if (getConfig().contains("bot-token")) {
            discordAPI = new DiscordApiBuilder().setToken(getConfig().getString("bot-token")).login().join();
        }

        BukkitCommandManager manager = new BukkitCommandManager(this);

        manager.getCommandCompletions().registerCompletion("chatchannel", (context) -> {
            ArrayList<String> completions = new ArrayList<>();

            for (ChatChannel chatChannel : channels) {
                if (chatChannel.canPlayerUse(context.getPlayer())) {
                    completions.add(chatChannel.getName());
                }
            }

            return completions;
        });

        manager.getCommandContexts().registerContext(ChatChannel.class, (context) -> {
            String name = context.popFirstArg();

            for (ChatChannel chatChannel : channels) {
                if (chatChannel.getName().equalsIgnoreCase(name)) {
                    return chatChannel;
                }
            }

            return null;
        });

        manager.getCommandConditions().addCondition(ChatChannel.class,"canuse", (context, execution, value) -> {
            if (!value.canPlayerUse(context.getIssuer().getPlayer())) {
                throw new ConditionFailedException("You cannot use that channel!");
            }
        });

        for (String key : getConfig().getConfigurationSection("channels").getKeys(false)) {
            SimpleChatChannel.Builder builder = SimpleChatChannel.builder(key);

            ConfigurationSection section = getConfig().getConfigurationSection("channels").getConfigurationSection(key);

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
                builder.setColor(TextColor.valueOf(section.getString("color").toUpperCase()));
            }

            if (section.contains("ignorable")) {
                builder.setIgnorable(section.getBoolean("ignorable"));
            }

            if (section.contains("is-town-chat")) {
                builder.setIsTownChat(section.getBoolean("is-town-chat"));
            }

            if (section.contains("is-nation-chat")) {
                builder.setIsNationChat(section.getBoolean("is-nation-chat"));
            }

            if (section.contains("is-alliance-chat")) {
                builder.setIsAllianceChat(section.getBoolean("is-alliance-chat"));
            }

            if (section.contains("is-party-chat")) {
                builder.setIsPartyChat(section.getBoolean("is-party-chat"));
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

            ChatChannel channel = builder.build(this);

            if (channel.isTownChat() || channel.isNationChat() || channel.isAllianceChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) {
                    getLogger().warning("Towny related channel with name [" + channel.getName() + "] found, but Towny isn't installed. Skipping channel.");
                    continue;
                }
            }

            if (channel.isPartyChat()) {
                if (!Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
                    getLogger().warning("mcMMO related channel with name [" + channel.getName() + "] found, but mcMMO isn't installed. Skipping channel.");
                    continue;
                }
            }

            // TODO: register command for each channel
            channels.add(channel);
        }

        boolean hasTownChat = channels.stream().anyMatch(ChatChannel::isTownChat);
        boolean hasNationChat = channels.stream().anyMatch(ChatChannel::isNationChat);
        boolean hasAllianceChat = channels.stream().anyMatch(ChatChannel::isAllianceChat);
        boolean hasPartyChat = channels.stream().anyMatch(ChatChannel::isPartyChat);

        if (!hasTownChat) {
            getLogger().info("Towny installed but no Town channel is setup!");
        }

        if (!hasNationChat) {
            getLogger().info("Towny installed but no Nation channel is setup!");
        }

        if (!hasAllianceChat) {
            getLogger().info("Towny installed but no Alliance channel is setup!");
        }

        if (!hasPartyChat) {
            getLogger().info("mcMMO installed but no Party channel is setup!");
        }

        manager.registerCommand(new ChannelCommand(this));
        manager.registerCommand(new ToggleCommand(this));

        setupListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
    }

    public boolean playerHasPlayerIgnored(Player player, OfflinePlayer target) {
        ArrayList<UUID> ignores = userIgnores.get(player.getUniqueId());

        if (ignores == null) {
            return false;
        }

        return ignores.contains(target.getUniqueId());
    }

    public boolean togglePlayerIgnoringPlayer(Player player, OfflinePlayer target) {
        ArrayList<UUID> ignores = userIgnores.computeIfAbsent(player.getUniqueId(), (uuid) -> new ArrayList<>());

        if (ignores.contains(target.getUniqueId())) {
            ignores.remove(target.getUniqueId());
            return false;
        } else {
            ignores.add(target.getUniqueId());
            return true;
        }
    }

    public boolean playerHasChannelMuted(Player player, ChatChannel chatChannel) {
        return getUserChannelMutes(player).contains(chatChannel.getName());
    }

    public boolean togglePlayerChannelMute(Player player, ChatChannel chatChannel) {
        ArrayList<String> toggles = getUserChannelMutes(player);

        if (toggles.contains(chatChannel.getName())) {
            toggles.remove(chatChannel.getName());
            return false;
        } else {
            toggles.add(chatChannel.getName());
            return true;
        }
    }

    public ArrayList<String> getUserChannelMutes(Player player) {
        return userToggles.computeIfAbsent(player.getUniqueId(), (uuid) -> new ArrayList<>());
    }

    public ChatChannel getPlayerChannel(Player player) {
        return userChannels.computeIfAbsent(player.getUniqueId(), (uuid) -> getDefaultChannel());
    }

    public void setPlayerChannel(Player player, ChatChannel chatChannel) {
        userChannels.put(player.getUniqueId(), chatChannel);
    }

    public ChatChannel getDefaultChannel() {
        for (ChatChannel channel : channels) {
            if (channel.isDefault()) {
                return channel;
            }
        }

        return null;
    }

    public Permission getPermission() {
        return permission;
    }

    public DiscordApi getDiscordAPI() {
        return discordAPI;
    }
}
