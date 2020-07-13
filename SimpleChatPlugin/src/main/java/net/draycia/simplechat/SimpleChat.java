package net.draycia.simplechat;

import de.themoep.minedown.MineDown;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.listeners.*;
import net.draycia.simplechat.managers.*;
import net.draycia.simplechat.storage.UserService;
import net.draycia.simplechat.storage.impl.JSONUserService;
import net.draycia.simplechat.storage.impl.MySQLUserService;
import net.draycia.simplechat.util.ItemStackUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Pattern;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();
    private ArrayList<Pattern> itemPatterns = new ArrayList<>();

    private Permission permission;

    private PluginMessageManager pluginMessageManager;
    private CommandManager commandManager;
    private ChannelManager channelManager;
    private RedisManager redisManager = null;

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
        commandManager = new CommandManager(this);
        channelManager = new ChannelManager(this);

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
        reloadPatterns();
    }

    @Override
    public void onDisable() {
        userService.cleanUp();
    }

    private void setupListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Register chat listeners
        pluginManager.registerEvents(new BukkitChatListener(this), this);
        pluginManager.registerEvents(new ItemLinkHandler(this), this);
        pluginManager.registerEvents(new LegacyFormatHandler(), this);
        pluginManager.registerEvents(new OfflineNameHandler(), this);
        pluginManager.registerEvents(new PingHandler(this), this);
        pluginManager.registerEvents(new PlaceholderHandler(this), this);
        pluginManager.registerEvents(new EmptyChatHandler(this), this);
        pluginManager.registerEvents(new UserFormattingListener(), this);
    }

    public void reloadPatterns() {
        itemPatterns.clear();

        for (String entry : getConfig().getStringList("item-link-placeholders")) {
            itemPatterns.add(Pattern.compile(Pattern.quote(entry)));
        }
    }

    public Component processMessage(String input, String... placeholders) {
        switch (getConfig().getString("formatting.type", "minimessage").toLowerCase()) {
            case "minedown":
                return processMineDown(input, placeholders);
            case "minimessage-markdown":
                return MiniMessage.markdown().parse(input, placeholders);
            case "minimessage":
            default:
                return MiniMessage.get().parse(input, placeholders);
        }
    }

    private Component processMineDown(String input, String... placeholders) {
        return BungeeCordComponentSerializer.get().deserialize(MineDown.parse(input, placeholders));
    }

    public ChatChannel getDefaultChannel() {
        for (ChatChannel channel : channels) {
            if (channel.isDefault()) {
                return channel;
            }
        }

        return channels.get(0);
    }

    public ChatChannel getChannel(String name) {
        if (name == null) {
            return getDefaultChannel();
        }

        for (ChatChannel chatChannel : channels) {
            if (chatChannel.getName().equalsIgnoreCase(name)) {
                return chatChannel;
            }
        }

        return getDefaultChannel();
    }

    public List<ChatChannel> getChannels() {
        return channels;
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

    public UserService getUserService() {
        return userService;
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public ItemStackUtils getItemStackUtils() {
        return itemStackUtils;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }
}
