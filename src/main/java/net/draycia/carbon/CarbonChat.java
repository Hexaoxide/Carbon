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
    this.checkForBungee();

    CommandAPI.onEnable(this);
    final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

    // Ensure config is present to be modified by the user
    if (!(new File(this.getDataFolder(), "config.yml").exists())) {
      this.saveDefaultConfig();
    }

    if (!(new File(this.getDataFolder(), "moderation.yml").exists())) {
      this.saveResource("moderation.yml", false);
    }

    if (!(new File(this.getDataFolder(), "language.yml").exists())) {
      this.saveResource("language.yml", false);
    }

    if (!(new File(this.getDataFolder(), "commands.yml").exists())) {
      this.saveResource("commands.yml", false);
    }

    this.loadModConfig();
    this.loadLanguage();

    // Setup Adventure
    this.adventureManager = new AdventureManager(this);

    // Setup vault and permissions
    this.permission = this.getServer().getServicesManager().getRegistration(Permission.class).getProvider();

    // Initialize managers
    this.channelManager = new ChannelManager(this);
    this.contextManager = new ContextManager();
    this.messageManager = new MessageManager(this);
    this.commandManager = new CommandManager(this);

    final String storageType = this.getConfig().getString("storage.type");

    if (storageType.equalsIgnoreCase("mysql")) {
      this.getLogger().info("Enabling MySQL storage!");
      this.userService = new MySQLUserService(this);
    } else if (storageType.equalsIgnoreCase("json")) {
      this.getLogger().info("Enabling JSON storage!");
      this.userService = new JSONUserService(this);
    } else {
      this.getLogger().warning("Invalid storage type selected! Falling back to JSON.");
      this.userService = new JSONUserService(this);
    }

    // Setup listeners
    this.setupListeners();
    this.registerContexts();

    new CarbonPlaceholders(this).register();

    if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
      this.getServer().getPluginManager().registerEvents(new TownyContext(this), this);
    }

    if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
      this.getServer().getPluginManager().registerEvents(new mcMMOContext(this), this);
    }

    if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
      this.getServer().getPluginManager().registerEvents(new WorldGuardContext(this), this);
    }

    if (Bukkit.getServicesManager().isProvidedFor(Economy.class)) {
      Bukkit.getPluginManager().registerEvents(new EconomyContext(this), this);
    }

    if (!FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
      this.getLogger().warning("Item linking disabled! Please use Paper 1.16.2 #172 or newer.");
    }
  }

  @Override
  public void onDisable() {
    this.userService.onDisable();
  }

  private void setupListeners() {
    final PluginManager pluginManager = this.getServer().getPluginManager();

    this.filterHandler = new FilterHandler(this);

    // Register chat listeners
    pluginManager.registerEvents(new BukkitChatListener(this), this);
    pluginManager.registerEvents(new CapsHandler(this), this);
    pluginManager.registerEvents(new CustomPlaceholderHandler(this), this);
    pluginManager.registerEvents(this.filterHandler, this);
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

    this.loadModConfig();
    this.loadLanguage();
    this.loadCommandsConfig();
  }

  public void reloadFilters() {
    this.filterHandler.reloadFilters();
  }

  private boolean bungeeEnabled = false;

  private void checkForBungee() {
    try {
      final Class<?> spigotConfig = Class.forName("org.spigotmc.Spigotconfig");
      final Field bungee = spigotConfig.getField("bungee");
      this.bungeeEnabled = bungee.getBoolean(null);
    } catch (final ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
    }
  }

  public boolean bungeeEnabled() {
    return this.bungeeEnabled;
  }

  private void loadModConfig() {
    this.modConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "moderation.yml"));
  }

  private void loadLanguage() {
    this.languageConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "language.yml"));
  }

  private void loadCommandsConfig() {
    this.commandsConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "commands.yml"));
  }

  private void registerContexts() {
    this.contextManager().register("distance", new DistanceContext());
  }

  @NonNull
  public YamlConfiguration moderationConfig() {
    return this.modConfig;
  }

  @NonNull
  public YamlConfiguration commandsConfig() {
    return this.commandsConfig;
  }

  @NonNull
  public YamlConfiguration language() {
    return this.languageConfig;
  }

  @NonNull
  public Permission permission() {
    return this.permission;
  }

  @NonNull
  public CommandManager commandManager() {
    return this.commandManager;
  }

  @NonNull
  public ChannelManager channelManager() {
    return this.channelManager;
  }

  @NonNull
  public MessageManager messageManager() {
    return this.messageManager;
  }

  @NonNull
  public UserService userService() {
    return this.userService;
  }

  @NonNull
  public AdventureManager adventureManager() {
    return this.adventureManager;
  }

  @NonNull
  public ContextManager contextManager() {
    return this.contextManager;
  }
}
