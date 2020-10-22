package net.draycia.carbon.bukkit;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.draycia.carbon.bukkit.listeners.events.BukkitChatListener;
import net.draycia.carbon.bukkit.listeners.events.ItemLinkHandler;
import net.draycia.carbon.bukkit.messaging.BungeeMessageService;
import net.draycia.carbon.bukkit.users.BukkitConsoleUser;
import net.draycia.carbon.bukkit.users.BukkitPlayerUser;
import net.draycia.carbon.bukkit.util.CarbonPlaceholders;
import net.draycia.carbon.bukkit.util.FunctionalityConstants;
import net.draycia.carbon.bukkit.util.Metrics;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.config.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.CarbonSettings;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.api.config.MessagingType;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.config.StorageType;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.channels.ChannelManager;
import net.draycia.carbon.common.config.KeySerializer;
import net.draycia.carbon.common.config.SoundSerializer;
import net.draycia.carbon.common.messaging.EmptyMessageService;
import net.draycia.carbon.api.config.SQLCredentials;
import net.draycia.carbon.common.messaging.RedisMessageService;
import net.draycia.carbon.bukkit.listeners.contexts.DistanceContext;
import net.draycia.carbon.bukkit.listeners.contexts.EconomyContext;
import net.draycia.carbon.common.contexts.FilterContext;
import net.draycia.carbon.bukkit.listeners.contexts.PAPIContext;
import net.draycia.carbon.bukkit.listeners.contexts.TownyContext;
import net.draycia.carbon.bukkit.listeners.contexts.WorldGuardContext;
import net.draycia.carbon.bukkit.listeners.contexts.mcMMOContext;
import net.draycia.carbon.common.listeners.events.CapsHandler;
import net.draycia.carbon.common.listeners.events.CustomPlaceholderHandler;
import net.draycia.carbon.common.listeners.events.IgnoredPlayerHandler;
import net.draycia.carbon.common.listeners.events.LegacyFormatHandler;
import net.draycia.carbon.common.listeners.events.MuteHandler;
import net.draycia.carbon.common.listeners.events.PingHandler;
import net.draycia.carbon.bukkit.listeners.events.PlaceholderHandler;
import net.draycia.carbon.common.listeners.events.PlayerJoinListener;
import net.draycia.carbon.bukkit.listeners.events.RelationalPlaceholderHandler;
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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.craftbukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("initialization.fields.uninitialized")
public final class CarbonChatBukkit extends JavaPlugin implements CarbonChat {

  private static final int BSTATS_PLUGIN_ID = 8720;

  private ChannelManager channelManager;
  private AdventureManager messageProcessor;
  private CommandSettingsRegistry commandSettings;
  private ModerationSettings moderationSettings;
  private CarbonSettings carbonSettings;
  private ChannelSettings channelSettings;
  private UserService<BukkitPlayerUser> userService;
  private MessageManager messageManager;
  private CarbonTranslations translations;
  private CommandManager<CarbonUser> commandManager;

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
  }

  @Override
  public void onEnable() {
    this.logger = LoggerFactory.getLogger(this.getName());

    // Setup metrics
    final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

    this.reloadConfig();

    // Setup Adventure
    final BukkitAudiences audiences = BukkitAudiences.create(this);
    this.messageProcessor = new AdventureManager(audiences, this.carbonSettings.formatType());

    // Initialize managers
    this.channelManager = new ChannelManager(this);

    // Handle messaging service
    final MessagingType messagingType = this.carbonSettings().messagingType();
    final MessageService messageService;

    if (messagingType == MessagingType.REDIS) {
      messageService = new RedisMessageService(this, this.carbonSettings.redisCredentials());
    } else if (messagingType == MessagingType.BUNGEECORD) {
      messageService = new BungeeMessageService(this); // TODO: only support this if bukkit
    } else {
      messageService = new EmptyMessageService();
    }

    this.messageManager = new MessageManager(this, messageService);

    // Handle storage service
    final StorageType storageType = this.carbonSettings().storageType();

    final Supplier<@NonNull Iterable<@NonNull BukkitPlayerUser>> supplier = () -> {
      final List<BukkitPlayerUser> users = new ArrayList<>();

      for (final Player player : Bukkit.getOnlinePlayers()) {
        users.add(this.userService.wrap(player.getUniqueId()));
      }

      return users;
    };

    final Function<String, UUID> nameResolver = name -> Bukkit.getOfflinePlayer(name).getUniqueId();

    if (storageType == StorageType.MYSQL) {
      this.logger().info("Enabling MySQL storage!");
      final SQLCredentials credentials = this.carbonSettings().sqlCredentials();
      this.userService = new MySQLUserService<>(this, credentials, supplier, BukkitPlayerUser::new,
        nameResolver, BukkitConsoleUser::new);
    } else if (storageType == StorageType.JSON) {
      this.logger().info("Enabling JSON storage!");
      this.userService = new JSONUserService<>(BukkitPlayerUser.class, this, supplier, BukkitPlayerUser::new,
        nameResolver, BukkitConsoleUser::new);
    } else {
      this.logger().error("Invalid storage type selected! Falling back to JSON.");
      this.userService = new JSONUserService<>(BukkitPlayerUser.class, this, supplier, BukkitPlayerUser::new,
        nameResolver, BukkitConsoleUser::new);
    }

    // Setup listeners
    this.setupCommands();
    this.setupListeners();
    this.registerContexts();

    // Setup PlaceholderAPI placeholders
    new CarbonPlaceholders(this).register();

    // Log missing functionality
    if (this.carbonSettings().showTips() && !FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
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

  @SuppressWarnings("return.type.incompatible")
  private void setupCommands() {
    try {
      this.commandManager = new PaperCommandManager<>(this,
        CommandExecutionCoordinator
          .simpleCoordinator(), sender -> {
        if (sender instanceof Player) {
          return this.userService().wrap(((Player) sender).getUniqueId());
        } else {
          return new BukkitConsoleUser((ConsoleCommandSender) sender);
        }
      }, user -> {
        if (user instanceof PlayerUser) {
          return Bukkit.getPlayer(((PlayerUser) user).uuid());
        } else {
          return Bukkit.getConsoleSender();
        }
      });

      CommandRegistrar.registerCommands(this.commandManager);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void reloadConfig() {
    this.getDataFolder().mkdirs();

    this.loadCarbonSettings();
    this.loadLanguage();
    this.loadModerationSettings();
    this.loadChannelSettings();
    this.loadCommandSettings();
  }

  private void loadCarbonSettings() {
    try {
      final HoconConfigurationLoader loader =
        this.loadConfigFile("config.conf", false);
      final CommentedConfigurationNode node = loader.load();

      this.carbonSettings = CarbonSettings.loadFrom(node);
      loader.save(node);
    } catch (final ConfigurateException exception) {
      exception.printStackTrace();
    }
  }

  private void loadLanguage() {
    try {
      final HoconConfigurationLoader loader =
        this.loadConfigFile("language.conf", false);
      final CommentedConfigurationNode node = loader.load();

      this.translations = CarbonTranslations.loadFrom(node);
      loader.save(node);
    } catch (final ConfigurateException exception) {
      exception.printStackTrace();
    }
  }

  private void loadModerationSettings() {
    try {
      final HoconConfigurationLoader loader =
        this.loadConfigFile("moderation.conf", false);
      final CommentedConfigurationNode node = loader.load();

      this.moderationSettings = ModerationSettings.loadFrom(node);
      loader.save(node);
    } catch (final ConfigurateException exception) {
      exception.printStackTrace();
    }
  }

  private void loadCommandSettings() {
    try {
      final HoconConfigurationLoader loader =
        this.loadConfigFile("carbonCommands.conf", true);
      final CommentedConfigurationNode node = loader.load();

      this.commandSettings = CommandSettingsRegistry.loadFrom(node);
      loader.save(node);
    } catch (final ConfigurateException exception) {
      exception.printStackTrace();
    }
  }

  private void loadChannelSettings() {
    try {
      final HoconConfigurationLoader loader =
        this.loadConfigFile("channels.conf", false);
      final CommentedConfigurationNode node = loader.load();

      this.channelSettings = ChannelSettings.loadFrom(node);
      loader.save(node);
    } catch (final ConfigurateException exception) {
      exception.printStackTrace();
    }
  }

  private HoconConfigurationLoader loadConfigFile(final String fileName, final boolean saveResource) {
    final File configFile = new File(this.getDataFolder(), fileName);

    if (!(configFile.exists()) && saveResource) {
      this.saveResource(fileName, false);
    }

    return HoconConfigurationLoader.builder()
      .defaultOptions(opts -> {
        return opts.shouldCopyDefaults(true).serializers(builder -> {
          builder.register(Key.class, KeySerializer.INSTANCE)
            .register(Sound.class, SoundSerializer.INSTANCE);
        });
      })
      .file(configFile)
      .build();
  }

  private void registerContexts() {
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
    new FilterContext(this);
    new PAPIContext();
  }

  public @NonNull MessageManager messageManager() {
    return this.messageManager;
  }

  public @NonNull UserService<BukkitPlayerUser> userService() {
    return this.userService;
  }

  @Override
  public @NonNull ChannelRegistry channelRegistry() {
    return this.channelManager.registry();
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
  public @NonNull CarbonSettings carbonSettings() {
    return this.carbonSettings;
  }

  @Override
  public @NonNull ChannelSettings channelSettings() {
    return this.channelSettings;
  }

  @Override
  public @NonNull CommandSettingsRegistry commandSettings() {
    return this.commandSettings;
  }

  @Override
  public @NonNull CommandManager<CarbonUser> commandManager() {
    return this.commandManager;
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

  @Override
  public @NonNull GsonComponentSerializer gsonSerializer() {
    return BukkitComponentSerializer.gson();
  }

}
