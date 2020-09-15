package net.draycia.carbon;

import net.draycia.carbon.listeners.contexts.DistanceContext;
import net.draycia.carbon.listeners.contexts.EconomyContext;
import net.draycia.carbon.listeners.contexts.FilterContext;
import net.draycia.carbon.listeners.contexts.TownyContext;
import net.draycia.carbon.listeners.contexts.WorldGuardContext;
import net.draycia.carbon.listeners.contexts.mcMMOContext;
import net.draycia.carbon.listeners.events.BukkitChatListener;
import net.draycia.carbon.listeners.events.CapsHandler;
import net.draycia.carbon.listeners.events.CustomPlaceholderHandler;
import net.draycia.carbon.listeners.events.IgnoredPlayerHandler;
import net.draycia.carbon.listeners.events.ItemLinkHandler;
import net.draycia.carbon.listeners.events.LegacyFormatHandler;
import net.draycia.carbon.listeners.events.MuteHandler;
import net.draycia.carbon.listeners.events.OfflineNameHandler;
import net.draycia.carbon.listeners.events.PingHandler;
import net.draycia.carbon.listeners.events.PlaceholderHandler;
import net.draycia.carbon.listeners.events.PlayerJoinListener;
import net.draycia.carbon.listeners.events.RelationalPlaceholderHandler;
import net.draycia.carbon.listeners.events.ShadowMuteHandler;
import net.draycia.carbon.listeners.events.UrlLinkHandler;
import net.draycia.carbon.listeners.events.UserFormattingHandler;
import net.draycia.carbon.listeners.events.WhisperPingHandler;
import net.draycia.carbon.managers.AdventureManager;
import net.draycia.carbon.managers.ChannelManager;
import net.draycia.carbon.managers.CommandManager;
import net.draycia.carbon.messaging.MessageManager;
import net.draycia.carbon.api.users.UserService;
import net.draycia.carbon.storage.impl.JSONUserService;
import net.draycia.carbon.storage.impl.MySQLUserService;
import net.draycia.carbon.util.CarbonPlaceholders;
import net.draycia.carbon.util.FunctionalityConstants;
import net.draycia.carbon.util.Metrics;
import dev.jorel.commandapi.CommandAPI;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

public final class CarbonChat extends JavaPlugin {

  private static final int BSTATS_PLUGIN_ID = 8720;

  private Permission permission;

  private CommandManager commandManager;
  private ChannelManager channelManager;
  private AdventureManager adventureManager;

  private UserService userService;
  private MessageManager messageManager;

  private YamlConfiguration modConfig;
  private YamlConfiguration languageConfig;
  private YamlConfiguration commandsConfig;

  private FilterContext filterContext;

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

    // Register chat listeners
    pluginManager.registerEvents(new BukkitChatListener(this), this);
    new CapsHandler(this);
    new CustomPlaceholderHandler(this);
    new IgnoredPlayerHandler();
    new ItemLinkHandler();
    new LegacyFormatHandler();
    new MuteHandler();
    new OfflineNameHandler();
    new PingHandler(this);
    new PlaceholderHandler();
    pluginManager.registerEvents(new PlayerJoinListener(this), this);
    new RelationalPlaceholderHandler();
    new ShadowMuteHandler(this);
    new UrlLinkHandler();
    new UserFormattingHandler();
    new WhisperPingHandler(this);
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();

    this.loadModConfig();
    this.loadLanguage();
    this.loadCommandsConfig();
  }

  public void reloadFilters() {
    this.filterContext.reloadFilters();
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
    this.filterContext = new FilterContext(this);

    if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
      this.getServer().getPluginManager().registerEvents(new TownyContext(this), this);
    }

    if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
      this.getServer().getPluginManager().registerEvents(new mcMMOContext(this), this);
    }

    if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
      new WorldGuardContext();
    }

    if (Bukkit.getServicesManager().isProvidedFor(Economy.class)) {
      new EconomyContext(this);
    }

    new DistanceContext();
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
}
