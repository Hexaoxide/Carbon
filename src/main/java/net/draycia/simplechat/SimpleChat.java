package net.draycia.simplechat;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.listeners.PlayerListener;
import net.draycia.simplechat.listeners.VoteListener;
import net.draycia.simplechat.managers.*;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.util.ItemStackUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();

    private Permission permission;

    private PluginMessageManager pluginMessageManager;
    private DiscordManager discordManager;
    private CommandManager commandManager;
    private ChannelManager channelManager;

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

        // Setup listeners
        setupListeners();

        // TODO: initialize user data handlers
    }

    @Override
    public void onDisable() {
        // TODO: save user data
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("Votifier")) {
            getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        }
    }

    public void sendPlayerPrivateMessage(Player sender, OfflinePlayer target, String message) {
        ChatUser user = UserManager.wrap(sender);
        ChatUser targetUser = UserManager.wrap(target.getUniqueId());

        if (user.isIgnoringUser(target.getUniqueId())) {
            return;
        }

        String toPlayerFormat = getConfig().getString("language.message-to-other");
        String fromPlayerFormat = getConfig().getString("language.message-from-other");

        Component toPlayerComponent = MiniMessage.instance().parse(toPlayerFormat, "message", message,
                "target", target.getName());

        Component fromPlayerComponent = MiniMessage.instance().parse(fromPlayerFormat, "message", message,
                "sender", sender.getName());

        user.asAudience().sendMessage(toPlayerComponent);

        if (user.isShadowMuted()) {
            return;
        }

        if (target.isOnline()) {
            targetUser.asAudience().sendMessage(fromPlayerComponent);

            user.setReplyTarget(targetUser.getUUID());
            targetUser.setReplyTarget(user.getUUID());

            if (getConfig().getBoolean("pings.on-whisper")) {
                Key key = Key.of(getConfig().getString("pings.sound"));
                Sound.Source source = Sound.Source.valueOf(getConfig().getString("pings.source"));
                float volume = (float)getConfig().getDouble("pings.volume");
                float pitch = (float)getConfig().getDouble("pings.pitch");

                targetUser.asAudience().playSound(Sound.of(key, source, volume, pitch));
            }
        } else {
            // TODO: cross server msg support, don't forget to include /ignore support
        }
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

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public ItemStackUtils getItemStackUtils() {
        return itemStackUtils;
    }
}
