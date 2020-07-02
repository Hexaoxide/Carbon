package net.draycia.simplechat;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.listeners.PlayerListener;
import net.draycia.simplechat.listeners.VoteListener;
import net.draycia.simplechat.listeners.chat.*;
import net.draycia.simplechat.managers.*;
import net.draycia.simplechat.storage.UserService;
import net.draycia.simplechat.util.ItemStackUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();

    private Permission permission;

    private PluginMessageManager pluginMessageManager;
    private DiscordManager discordManager;
    private CommandManager commandManager;
    private ChannelManager channelManager;

    private UserService userService;

    private ItemStackUtils itemStackUtils;

    private BukkitAudiences audiences;

    @Override
    public void onEnable() {
        // Setup Adventure
        audiences = BukkitAudiences.create(this);

        try {
            itemStackUtils = new ItemStackUtils();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // Setup vault and permissions
        permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();

        // Ensure config is present to be modified by the user
        saveDefaultConfig();

        // Initialize managers
        pluginMessageManager = new PluginMessageManager(this);
        discordManager = new DiscordManager(this);
        commandManager = new CommandManager(this);
        channelManager = new ChannelManager(this);

        // TODO: initialize UserService

        // Setup listeners
        setupListeners();
    }

    @Override
    public void onDisable() {
        // TODO: save user data
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerListener(this), this);

        if (pluginManager.isPluginEnabled("Votifier")) {
            pluginManager.registerEvents(new VoteListener(this), this);
        }

        // Register chat listeners
        pluginManager.registerEvents(new FilterHandler(this), this);
        pluginManager.registerEvents(new ItemLinkHandler(this), this);
        pluginManager.registerEvents(new LegacyFormatHandler(), this);
        pluginManager.registerEvents(new OfflineNameHandler(), this);
        pluginManager.registerEvents(new PingHandler(this), this);
        pluginManager.registerEvents(new PlaceholderHandler(this), this);
        pluginManager.registerEvents(new ShadowMuteHandler(), this);
    }

    public ChatChannel getDefaultChannel() {
        for (ChatChannel channel : channels) {
            if (channel.isDefault()) {
                return channel;
            }
        }

        return null;
    }

    public ChatChannel getChannel(String name) {
        for (ChatChannel chatChannel : channels) {
            if (chatChannel.getName().equalsIgnoreCase(name)) {
                return chatChannel;
            }
        }

        return null;
    }

    public List<ChatChannel> getChannels() {
        return channels;
    }

    public Permission getPermission() {
        return permission;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public PluginMessageManager getPluginMessageManager() {
        return pluginMessageManager;
    }

    public UserService getUserService() {
        return userService;
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public ItemStackUtils getItemStackUtils() {
        return itemStackUtils;
    }
}
