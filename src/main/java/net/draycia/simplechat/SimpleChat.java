package net.draycia.simplechat;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.SimpleChatChannel;
import net.draycia.simplechat.commands.ChannelCommand;
import net.draycia.simplechat.commands.ToggleCommand;
import net.draycia.simplechat.listeners.PlayerChatListener;
import net.kyori.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();

    private HashMap<UUID, ChatChannel> userChannels = new HashMap<>();
    private HashMap<UUID, ArrayList<String>> userToggles = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

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

            if (section.contains("format")) {
                builder.setFormat(section.getString("format"));
            }

            if (section.contains("format-staff")) {
                builder.setStaffFormat(section.getString("format-staff"));
            }

            if (section.contains("webhook")) {
                builder.setWebhook(section.getString("webhook"));
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
                builder.setIsAllianceChat(section.getBoolean("is-party-chat"));
            }

            if (section.contains("default")) {
                builder.setIsDefault(section.getBoolean("default"));
            }

            ChatChannel channel = builder.build(this);

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

}
