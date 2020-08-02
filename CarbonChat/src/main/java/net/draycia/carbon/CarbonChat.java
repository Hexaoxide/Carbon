package net.draycia.carbon;

import net.draycia.carbon.channels.contexts.impl.DistanceContext;
import net.draycia.carbon.listeners.*;
import net.draycia.carbon.managers.*;
import net.draycia.carbon.storage.UserService;
import net.draycia.carbon.storage.impl.JSONUserService;
import net.draycia.carbon.storage.impl.MySQLUserService;
import net.draycia.carbon.util.ItemStackUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CarbonChat extends JavaPlugin {

    private Permission permission;

    private PluginMessageManager pluginMessageManager;
    private CommandManager commandManager;
    private ChannelManager channelManager;
    private RedisManager redisManager = null;
    private AdventureManager adventureManager;
    private ContextManager contextManager;

    private UserService userService;

    private ItemStackUtils itemStackUtils;

    @Override
    public void onEnable() {
        // Setup Adventure
        adventureManager = new AdventureManager(this);

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
        commandManager = new CommandManager(this);
        channelManager = new ChannelManager(this);
        contextManager = new ContextManager();

        if (getConfig().getBoolean("redis.enabled")) {
            redisManager = new RedisManager(this);
        }

        String storageType = getConfig().getString("storage.type");

        if (storageType.equalsIgnoreCase("mysql")) {
            userService = new MySQLUserService(this);
        } else if (storageType.equalsIgnoreCase("json")) {
            userService = new JSONUserService(this);
        } else {
            getLogger().warning("Invalid storage type selected! Falling back to JSON.");
            userService = new JSONUserService(this);
        }

        // Setup listeners
        setupListeners();
        registerContexts();
    }

    @Override
    public void onDisable() {
        userService.cleanUp();
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Register chat listeners
        pluginManager.registerEvents(new BukkitChatListener(this), this);
        pluginManager.registerEvents(new EmptyChatHandler(this), this);
        pluginManager.registerEvents(new ItemLinkHandler(this), this);
        pluginManager.registerEvents(new LegacyFormatHandler(), this);
        pluginManager.registerEvents(new OfflineNameHandler(), this);
        pluginManager.registerEvents(new PingHandler(this), this);
        pluginManager.registerEvents(new PlaceholderHandler(), this);
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new UserFormattingListener(), this);
    }

    private void registerContexts() {
        getContextManager().register("distance", new DistanceContext());
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

    public PluginMessageManager getPluginMessageManager() {
        return pluginMessageManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
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
