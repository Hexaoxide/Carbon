package net.draycia.carbon;

import net.draycia.carbon.channels.contexts.impl.DistanceContext;
import net.draycia.carbon.channels.contexts.impl.TownyContext;
import net.draycia.carbon.channels.contexts.impl.WorldGuardContext;
import net.draycia.carbon.channels.contexts.impl.mcMMOContext;
import net.draycia.carbon.listeners.*;
import net.draycia.carbon.managers.*;
import net.draycia.carbon.messaging.MessageManager;
import net.draycia.carbon.storage.UserService;
import net.draycia.carbon.storage.impl.JSONUserService;
import net.draycia.carbon.storage.impl.MySQLUserService;
import net.draycia.carbon.util.CarbonPlaceholders;
import net.draycia.carbon.util.ItemStackUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CarbonChat extends JavaPlugin {

    private Permission permission;

    private CommandManager commandManager;
    private ChannelManager channelManager;
    private AdventureManager adventureManager;
    private ContextManager contextManager;

    private UserService userService;
    private MessageManager messageManager;

    private ItemStackUtils itemStackUtils;

    private YamlConfiguration modConfig;

    public static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().extractUrls().character('§').build();

    @Override
    public void onEnable() {
        // Ensure config is present to be modified by the user
        saveDefaultConfig();
        saveResource("moderation.yml", false);

        modConfig = new YamlConfiguration();

        try {
            modConfig.load(new File(getDataFolder(), "moderation.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // Setup Adventure
        adventureManager = new AdventureManager(this);

        try {
            itemStackUtils = new ItemStackUtils();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // Setup vault and permissions
        permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();

        // Initialize managers
        commandManager = new CommandManager(this);
        channelManager = new ChannelManager(this);
        contextManager = new ContextManager();
        messageManager = new MessageManager(this);

        String storageType = getConfig().getString("storage.type");

        if (storageType.equalsIgnoreCase("mysql")) {
            getLogger().info("Enabling MySQL storage!");
            userService = new MySQLUserService(this);
        } else if (storageType.equalsIgnoreCase("json")) {
            getLogger().info("Enabling JSON storage!");
            userService = new JSONUserService(this);
        } else {
            getLogger().warning("Invalid storage type selected! Falling back to JSON.");
            userService = new JSONUserService(this);
        }

        // Setup listeners
        setupListeners();
        registerContexts();

        new CarbonPlaceholders(this).register();

        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            getServer().getPluginManager().registerEvents(new TownyContext(this), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            getServer().getPluginManager().registerEvents(new mcMMOContext(this), this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            getServer().getPluginManager().registerEvents(new WorldGuardContext(this), this);
        }
    }

    @Override
    public void onDisable() {
        userService.onDisable();
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Register chat listeners
        pluginManager.registerEvents(new BukkitChatListener(this), this);
        pluginManager.registerEvents(new IgnoredPlayerHandler(), this);
        pluginManager.registerEvents(new ItemLinkHandler(this), this);
        pluginManager.registerEvents(new LegacyFormatHandler(), this);
        pluginManager.registerEvents(new OfflineNameHandler(), this);
        pluginManager.registerEvents(new PingHandler(this), this);
        pluginManager.registerEvents(new PlaceholderHandler(), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new RelationalPlaceholderHandler(), this);
        pluginManager.registerEvents(new UserFormattingListener(), this);
        pluginManager.registerEvents(new WhisperPingHandler(this), this);
        pluginManager.registerEvents(new RequiredBalanceHandler(this), this);

        pluginManager.registerEvents(new CapsHandler(this), this);
        pluginManager.registerEvents(new FilterHandler(this), this);
        pluginManager.registerEvents(new MuteHandler(), this);
        pluginManager.registerEvents(new ShadowMuteHandler(this), this);
    }

    private void registerContexts() {
        getContextManager().register("distance", new DistanceContext());
    }

    public YamlConfiguration getModConfig() {
        return modConfig;
    }

    public Permission getPermission() {
        return permission;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public UserService getUserService() {
        return userService;
    }

    public ItemStackUtils getItemStackUtils() {
        return itemStackUtils;
    }

    public AdventureManager getAdventureManager() {
        return adventureManager;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }
}
