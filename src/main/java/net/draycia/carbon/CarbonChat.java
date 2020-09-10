package net.draycia.carbon;

import dev.jorel.commandapi.CommandAPI;
import net.draycia.carbon.channels.contexts.impl.DistanceContext;
import net.draycia.carbon.channels.contexts.impl.EconomyContext;
import net.draycia.carbon.channels.contexts.impl.TownyContext;
import net.draycia.carbon.channels.contexts.impl.WorldGuardContext;
import net.draycia.carbon.channels.contexts.impl.mcMMOContext;
import net.draycia.carbon.listeners.BukkitChatListener;
import net.draycia.carbon.listeners.CapsHandler;
import net.draycia.carbon.listeners.CustomPlaceholderHandler;
import net.draycia.carbon.listeners.FilterHandler;
import net.draycia.carbon.listeners.IgnoredPlayerHandler;
import net.draycia.carbon.listeners.ItemLinkHandler;
import net.draycia.carbon.listeners.LegacyFormatHandler;
import net.draycia.carbon.listeners.MuteHandler;
import net.draycia.carbon.listeners.OfflineNameHandler;
import net.draycia.carbon.listeners.PingHandler;
import net.draycia.carbon.listeners.PlaceholderHandler;
import net.draycia.carbon.listeners.PlayerJoinListener;
import net.draycia.carbon.listeners.RelationalPlaceholderHandler;
import net.draycia.carbon.listeners.ShadowMuteHandler;
import net.draycia.carbon.listeners.UserFormattingHandler;
import net.draycia.carbon.listeners.WhisperPingHandler;
import net.draycia.carbon.managers.AdventureManager;
import net.draycia.carbon.managers.ChannelManager;
import net.draycia.carbon.managers.CommandManager;
import net.draycia.carbon.managers.ContextManager;
import net.draycia.carbon.messaging.MessageManager;
import net.draycia.carbon.storage.UserService;
import net.draycia.carbon.storage.impl.JSONUserService;
import net.draycia.carbon.storage.impl.MySQLUserService;
import net.draycia.carbon.util.CarbonPlaceholders;
import net.draycia.carbon.util.FunctionalityConstants;
import net.draycia.carbon.util.Metrics;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.lang.reflect.Field;

public final class CarbonChat extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID = 8720;

  private Permission permission;

  private CommandManager commandManager;
  private ChannelManager channelManager;
  private AdventureManager adventureManager;
  private ContextManager contextManager;

  private UserService userService;
  private MessageManager messageManager;

  private YamlConfiguration modConfig;
  private YamlConfiguration languageConfig;
  private YamlConfiguration commandsConfig;

  private FilterHandler filterHandler;

  public static final LegacyComponentSerializer LEGACY =
    LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .character('§')
      .useUnusualXRepeatedCharacterHexFormat()
      .build();

  @Override
  public void onLoad() {
    CommandAPI.onLoad(false);
  }

  @Override
  public void onEnable() {
    checkForBungee();

    CommandAPI.onEnable(this);
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

    if (!(new File(getDataFolder(), "commands.yml").exists())) {
      saveResource("commands.yml", false);
    }

    loadModConfig();
    loadLanguage();

    // Setup Adventure
    adventureManager = new AdventureManager(this);

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

    if (!FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
      getLogger().warning("Item linking disabled! Please use Paper 1.16.2 #172 or newer.");
    }
  }

  @Override
  public void onDisable() {
    userService.onDisable();
  }

  private void setupListeners() {
    PluginManager pluginManager = getServer().getPluginManager();

    filterHandler = new FilterHandler(this);

    // Register chat listeners
    pluginManager.registerEvents(new BukkitChatListener(this), this);
    pluginManager.registerEvents(new CapsHandler(this), this);
    pluginManager.registerEvents(new CustomPlaceholderHandler(this), this);
    pluginManager.registerEvents(filterHandler, this);
    pluginManager.registerEvents(new IgnoredPlayerHandler(), this);
    pluginManager.registerEvents(new ItemLinkHandler(), this);
    pluginManager.registerEvents(new LegacyFormatHandler(), this);
    pluginManager.registerEvents(new MuteHandler(), this);
    pluginManager.registerEvents(new OfflineNameHandler(), this);
    pluginManager.registerEvents(new PingHandler(this), this);
    pluginManager.registerEvents(new PlaceholderHandler(), this);
    pluginManager.registerEvents(new PlayerJoinListener(this), this);
    pluginManager.registerEvents(new RelationalPlaceholderHandler(), this);
    pluginManager.registerEvents(new ShadowMuteHandler(this), this);
    pluginManager.registerEvents(new UserFormattingHandler(), this);
    pluginManager.registerEvents(new WhisperPingHandler(this), this);

  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();

    loadModConfig();
    loadLanguage();
    loadCommandsConfig();
  }

  public void reloadFilters() {
    filterHandler.reloadFilters();
  }

  private boolean bungeeEnabled = false;

  private void checkForBungee() {
    try {
      Class<?> spigotConfig = Class.forName("org.spigotmc.Spigotconfig");
      Field bungee = spigotConfig.getField("bungee");
      bungeeEnabled = bungee.getBoolean(null);
    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
    }
  }

  public boolean isBungeeEnabled() {
    return bungeeEnabled;
  }

  private void loadModConfig() {
    modConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "moderation.yml"));
  }

  private void loadLanguage() {
    languageConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "language.yml"));
  }

  private void loadCommandsConfig() {
    commandsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "commands.yml"));
  }

  private void registerContexts() {
    getContextManager().register("distance", new DistanceContext());
  }

  @NonNull
  public YamlConfiguration getModConfig() {
    return modConfig;
  }

  @NonNull
  public YamlConfiguration getCommandsConfig() {
    return commandsConfig;
  }

  @NonNull
  public YamlConfiguration getLanguage() {
    return languageConfig;
  }

  @NonNull
  public Permission getPermission() {
    return permission;
  }

  @NonNull
  public CommandManager getCommandManager() {
    return commandManager;
  }

  @NonNull
  public ChannelManager getChannelManager() {
    return channelManager;
  }

  @NonNull
  public MessageManager getMessageManager() {
    return messageManager;
  }

  @NonNull
  public UserService getUserService() {
    return userService;
  }

  @NonNull
  public AdventureManager getAdventureManager() {
    return adventureManager;
  }

  @NonNull
  public ContextManager getContextManager() {
    return contextManager;
  }
}
