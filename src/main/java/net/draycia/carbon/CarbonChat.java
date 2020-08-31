package net.draycia.carbon;

import net.draycia.carbon.channels.contexts.impl.EconomyContext;
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
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.Metrics;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CarbonChat extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 8720;

    private Permission permission;

    private CommandManager commandManager;
    private ChannelManager channelManager;
    private AdventureManager adventureManager;
    private ContextManager contextManager;

    private UserService userService;
    private MessageManager messageManager;

    private CarbonUtils itemStackUtils;

    private YamlConfiguration modConfig;
    private YamlConfiguration languageConfig;

    public static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .extractUrls()
                    .hexColors()
                    .character('§')
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        // Ensure config is present to be modified by the user
        if (!(new File(getDataFolder(), "config.yml").exists())) {
            saveDefaultConfig();
        }

        if (!(new File(getDataFolder(), "moderation.yml").exists())) {
            saveResource("moderation.yml", false);
        }

        if (!(new File(getDataFolder(), "language.yml").exists())) {
            saveResource("language.yml", false);
        }

        loadModConfig();
        loadLanguage();

        // Setup Adventure
        adventureManager = new AdventureManager(this);

        try {
            itemStackUtils = new CarbonUtils();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // Setup vault and permissions
        permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();

        // Initialize managers
        channelManager = new ChannelManager(this);
        contextManager = new ContextManager();
        messageManager = new MessageManager(this);
        commandManager = new CommandManager(this);

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

        if (Bukkit.getServicesManager().isProvidedFor(Economy.class)) {
            Bukkit.getPluginManager().registerEvents(new EconomyContext(this), this);
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

        pluginManager.registerEvents(new CapsHandler(this), this);
        pluginManager.registerEvents(new FilterHandler(this), this);
        pluginManager.registerEvents(new MuteHandler(), this);
        pluginManager.registerEvents(new ShadowMuteHandler(this), this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        loadModConfig();
        loadLanguage();
    }

    private void loadModConfig() {
        modConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "moderation.yml"));
    }

    private void loadLanguage() {
        languageConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "language.yml"));
    }

    private void registerContexts() {
        getContextManager().register("distance", new DistanceContext());
    }

    public YamlConfiguration getModConfig() {
        return modConfig;
    }

    public YamlConfiguration getLanguage() {
        return languageConfig;
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

    public CarbonUtils getItemStackUtils() {
        return itemStackUtils;
    }

    public AdventureManager getAdventureManager() {
        return adventureManager;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }
}
