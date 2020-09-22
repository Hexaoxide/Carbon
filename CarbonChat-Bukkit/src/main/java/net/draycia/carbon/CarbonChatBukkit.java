package net.draycia.carbon;

import com.intellectualsites.commands.execution.CommandExecutionCoordinator;
import com.intellectualsites.commands.paper.PaperCommandManager;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.adventure.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.adventure.FormatType;
import net.draycia.carbon.common.messaging.EmptyMessageService;
import net.draycia.carbon.listeners.contexts.DistanceContext;
import net.draycia.carbon.listeners.contexts.EconomyContext;
import net.draycia.carbon.listeners.contexts.FilterContext;
import net.draycia.carbon.listeners.contexts.TownyContext;
import net.draycia.carbon.listeners.contexts.WorldGuardContext;
import net.draycia.carbon.listeners.contexts.mcMMOContext;
import net.draycia.carbon.listeners.events.BukkitChatListener;
import net.draycia.carbon.common.listeners.events.CapsHandler;
import net.draycia.carbon.common.listeners.events.CustomPlaceholderHandler;
import net.draycia.carbon.common.listeners.events.IgnoredPlayerHandler;
import net.draycia.carbon.listeners.events.ItemLinkHandler;
import net.draycia.carbon.common.listeners.events.LegacyFormatHandler;
import net.draycia.carbon.common.listeners.events.MuteHandler;
import net.draycia.carbon.common.listeners.events.PingHandler;
import net.draycia.carbon.listeners.events.PlaceholderHandler;
import net.draycia.carbon.common.listeners.events.PlayerJoinListener;
import net.draycia.carbon.listeners.events.RelationalPlaceholderHandler;
import net.draycia.carbon.common.listeners.events.ShadowMuteHandler;
import net.draycia.carbon.common.listeners.events.UrlLinkHandler;
import net.draycia.carbon.common.listeners.events.UserFormattingHandler;
import net.draycia.carbon.common.listeners.events.WhisperPingHandler;
import net.draycia.carbon.common.adventure.AdventureManager;
import net.draycia.carbon.common.commands.misc.CommandRegistrar;
import net.draycia.carbon.common.messaging.MessageManager;
import net.draycia.carbon.api.users.UserService;
import net.draycia.carbon.common.users.JSONUserService;
import net.draycia.carbon.common.users.MySQLUserService;
import net.draycia.carbon.storage.impl.BukkitChatUser;
import net.draycia.carbon.util.CarbonPlaceholders;
import net.draycia.carbon.util.FunctionalityConstants;
import net.draycia.carbon.util.Metrics;
import dev.jorel.commandapi.CommandAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {

  private static final int BSTATS_PLUGIN_ID = 8720;

  private Permission permission;

  private ChannelRegistry channelRegistry;
  private CommandSettingsRegistry commandSettings;
  private AdventureManager messageProcessor;

  private UserService<BukkitChatUser> userService;
  private MessageManager messageManager;

  private FilterContext filterContext;

  private CarbonTranslations translations;

  private Logger logger;

  public static final LegacyComponentSerializer LEGACY =
    LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .character('§')
      .useUnusualXRepeatedCharacterHexFormat()
      .build();

  @Override
  public void onLoad() {
    CarbonChatProvider.register(this);

    CommandAPI.onLoad(false);
  }

  @Override
  public void onEnable() {
    this.logger = LoggerFactory.getLogger(this.getName());

    CommandAPI.onEnable(this);
    final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

    // Ensure config is present to be modified by the user
    if (!(new File(this.getDataFolder(), "config.yml").exists())) {
      this.saveDefaultConfig();
    }

    if (!(new File(this.getDataFolder(), "moderation.yml").exists())) {
      this.saveResource("moderation.yml", false);
    }

    this.reloadConfig();

    // Setup Adventure
    this.messageProcessor = new AdventureManager(BukkitAudiences.create(this), FormatType.MINIMESSAGE); // TODO: get format type from config

    // Setup vault and permissions
    this.permission = this.getServer().getServicesManager().getRegistration(Permission.class).getProvider();

    // Initialize managers
    this.channelRegistry = new ChannelRegistry();
    this.messageManager = new MessageManager(this, new EmptyMessageService()); // TODO: get message service from config

    final String storageType = this.getConfig().getString("storage.type");

    if (storageType.equalsIgnoreCase("mysql")) {
      this.logger().info("Enabling MySQL storage!");
      this.userService = new MySQLUserService(this);
    } else if (storageType.equalsIgnoreCase("json")) {
      this.logger().info("Enabling JSON storage!");
      this.userService = new JSONUserService(this);
    } else {
      this.logger().error("Invalid storage type selected! Falling back to JSON.");
      this.userService = new JSONUserService(this);
    }

    // Setup listeners
    this.setupCommands();
    this.setupListeners();
    this.registerContexts();

    new CarbonPlaceholders(this).register();

    if (!FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
      this.logger().error("Item linking disabled! Please use Paper 1.16.2 #172 or newer.");
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

    new CapsHandler();
    new CustomPlaceholderHandler();
    new IgnoredPlayerHandler();
    new ItemLinkHandler();
    new LegacyFormatHandler();
    new MuteHandler();
    new PingHandler();
    new PlaceholderHandler();
    new PlayerJoinListener();
    new RelationalPlaceholderHandler();
    new ShadowMuteHandler();
    new UrlLinkHandler();
    new UserFormattingHandler();
    new WhisperPingHandler();
  }

  private void setupCommands() {
    try {
      final PaperCommandManager<ChatUser> manager = new PaperCommandManager<>(this,
        CommandExecutionCoordinator
          .simpleCoordinator(), sender -> {
        if (sender instanceof Player) {
          return this.userService().wrap(((Player) sender).getUniqueId());
        } else {
          // TODO: weeeeee fix this
          throw new IllegalArgumentException("Non-players not supported yet!");
        }
      }, user -> Bukkit.getPlayer(user.uuid()));

      CommandRegistrar.registerCommands(manager);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();

    this.loadLanguage();
    this.loadModerationSettings();
    this.loadCommandSettings();
  }

  private void loadLanguage() {
    final File languageFile = new File(this.getDataFolder(), "language.yml");
    final YamlConfigurationLoader languageLoader = YamlConfigurationLoader.builder().setFile(languageFile).build();

    try {
      final BasicConfigurationNode languageNode = languageLoader.load();
      this.translations = CarbonTranslations.loadFrom(languageNode);
      languageLoader.save(languageNode);
    } catch (final IOException | ObjectMappingException ignored) {
    }
  }

  private void loadCommandSettings() {
    // TODO: probably redo this from scratch
    if (!(new File(this.getDataFolder(), "carbon-commands.yml").exists())) {
      this.saveResource("carbon-commands.yml", false);
    }

    final File commandSettingsFile = new File(this.getDataFolder(), "carbon-commands.yml");
    final YamlConfigurationLoader commandSettingsLoader = YamlConfigurationLoader.builder().setFile(commandSettingsFile).build();

    try {
      final BasicConfigurationNode commandSettingsNode = commandSettingsLoader.load();
      this.commandSettings = CommandSettingsRegistry.loadFrom(commandSettingsNode);
      commandSettingsLoader.save(commandSettingsNode);
    } catch (final IOException | ObjectMappingException ignored) {
    }
  }

  private void loadModerationSettings() {

  }

  public void reloadFilters() {
    this.filterContext.reloadFilters();
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
  public Permission permission() {
    return this.permission;
  }

  @NonNull
  public MessageManager messageManager() {
    return this.messageManager;
  }

  @NonNull
  public UserService<BukkitChatUser> userService() {
    return this.userService;
  }

  @Override
  public @NonNull ChannelRegistry channelRegistry() {
    return this.channelRegistry;
  }

  @Override
  public @NonNull CommandSettingsRegistry commandSettingsRegistry() {
    return this.commandSettings;
  }

  @Override
  public @NonNull CarbonTranslations translations() {
    return this.translations;
  }

  @Override
  public @NonNull ModerationSettings moderationSettings() {
    return this.moderationSettings;
  }

  @Override
  public @NonNull MessageProcessor messageProcessor() {
    return this.messageProcessor;
  }

  @Override
  public @NonNull MessageService messageService() {
    return this.messageManager().messageService();
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  @Override
  public @NonNull Path dataFolder() {
    return this.getDataFolder().toPath();
  }

}
