package net.draycia.simplechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.listeners.PlayerListener;
import net.draycia.simplechat.managers.ChannelManager;
import net.draycia.simplechat.managers.CommandManager;
import net.draycia.simplechat.managers.DiscordManager;
import net.draycia.simplechat.managers.PluginMessageManager;
import net.draycia.simplechat.util.ItemStackUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SimpleChat extends JavaPlugin {

    private ArrayList<ChatChannel> channels = new ArrayList<>();

    private HashMap<UUID, ChatChannel> userChannels = new HashMap<>();
    private HashMap<UUID, ArrayList<String>> userToggles = new HashMap<>();
    private HashMap<UUID, ArrayList<UUID>> userIgnores = new HashMap<>();
    private ArrayList<UUID> shadowMutes = new ArrayList<>();

    private Permission permission;

    private PluginMessageManager pluginMessageManager;
    private DiscordManager discordManager;
    private CommandManager commandManager;
    private ChannelManager channelManager;

    private ItemStackUtils itemStackUtils;

    private BukkitAudiences audiences;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

        // Load userChannels
        File userChannelsFile = new File(getDataFolder(), "userchannels.json");

        try {
            if (!userChannelsFile.exists()) {
                userChannelsFile.getParentFile().mkdirs();
                userChannelsFile.createNewFile();
            } else {
                try (JsonReader reader = new JsonReader(new FileReader(userChannelsFile))) {
                    Type type = new TypeToken<HashMap<UUID, String>>(){}.getType();
                    HashMap<UUID, String> userChannelsBuffer = gson.fromJson(reader, type);

                    if (userChannelsBuffer != null) {
                        for (Map.Entry<UUID, String> entry : userChannelsBuffer.entrySet()) {
                            ChatChannel channel = getChannel(entry.getValue());

                            if (channel == null) {
                                continue;
                            }

                            userChannels.put(entry.getKey(), channel);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load userToggles
        File userTogglesFile = new File(getDataFolder(), "usertoggles.json");

        try {
            if (!userTogglesFile.exists()) {
                userTogglesFile.getParentFile().mkdirs();
                userTogglesFile.createNewFile();
            } else {
                try (JsonReader reader = new JsonReader(new FileReader(userTogglesFile))) {
                    Type type = new TypeToken<HashMap<UUID, ArrayList<String>>>(){}.getType();
                    HashMap<UUID, ArrayList<String>> userTogglesBuffer = gson.fromJson(reader, type);

                    if (userTogglesBuffer != null) {
                        userToggles = userTogglesBuffer;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load userIgnores
        File userIgnoresFile = new File(getDataFolder(), "userignores.json");

        try {
            if (!userIgnoresFile.exists()) {
                userIgnoresFile.getParentFile().mkdirs();
                userIgnoresFile.createNewFile();
            } else {
                try (JsonReader reader = new JsonReader(new FileReader(userIgnoresFile))) {
                    Type type = new TypeToken<HashMap<UUID, ArrayList<UUID>>>(){}.getType();
                    HashMap<UUID, ArrayList<UUID>> userIgnoresBuffer = gson.fromJson(reader, type);

                    if (userIgnoresBuffer != null) {
                        userIgnores = userIgnoresBuffer;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load shadowMutes
        File shadowMutesFile = new File(getDataFolder(), "shadowmutes.json");

        try {
            if (!shadowMutesFile.exists()) {
                shadowMutesFile.getParentFile().mkdirs();
                shadowMutesFile.createNewFile();
            } else {
                try (JsonReader reader = new JsonReader(new FileReader(shadowMutesFile))) {
                    Type type = new TypeToken<ArrayList<UUID>>(){}.getType();
                    ArrayList<UUID> shadowMutesBuffer = gson.fromJson(reader, type);

                    if (shadowMutesBuffer != null) {
                        shadowMutes = shadowMutesBuffer;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Save userChannels
        File userChannelsFile = new File(getDataFolder(), "userchannels.json");

        try {
            if (!userChannelsFile.exists()) {
                userChannelsFile.getParentFile().mkdirs();
                userChannelsFile.createNewFile();
            }

            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(userChannelsFile))) {
                Type type = new TypeToken<HashMap<UUID, String>>(){}.getType();

                HashMap<UUID, String> userChannelsBuffer = new HashMap<>();

                for (Map.Entry<UUID, ChatChannel> entry : userChannels.entrySet()) {
                    userChannelsBuffer.put(entry.getKey(), entry.getValue().getName());
                }

                gson.toJson(userChannelsBuffer, type, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save userToggles
        File userTogglesFile = new File(getDataFolder(), "usertoggles.json");

        try {
            if (!userTogglesFile.exists()) {
                userTogglesFile.getParentFile().mkdirs();
                userTogglesFile.createNewFile();
            }

            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(userTogglesFile))) {
                Type type = new TypeToken<HashMap<UUID, ArrayList<String>>>(){}.getType();
                gson.toJson(userToggles, type, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save userIgnores
        File userIgnoresFile = new File(getDataFolder(), "userignores.json");

        try {
            if (!userIgnoresFile.exists()) {
                userIgnoresFile.getParentFile().mkdirs();
                userIgnoresFile.createNewFile();
            }

            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(userIgnoresFile))) {
                Type type = new TypeToken<HashMap<UUID, ArrayList<UUID>>>(){}.getType();
                gson.toJson(userIgnores, type, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save shadowMutes
        File shadowMutesFile = new File(getDataFolder(), "shadowmutes.json");

        try {
            if (!shadowMutesFile.exists()) {
                shadowMutesFile.getParentFile().mkdirs();
                shadowMutesFile.createNewFile();
            }

            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(shadowMutesFile))) {
                Type type = new TypeToken<ArrayList<UUID>>(){}.getType();
                gson.toJson(shadowMutes, type, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public boolean playerHasPlayerIgnored(OfflinePlayer player, OfflinePlayer target) {
        ArrayList<UUID> ignores = userIgnores.get(player.getUniqueId());

        if (ignores == null) {
            return false;
        }

        return ignores.contains(target.getUniqueId());
    }

    public boolean togglePlayerIgnoringPlayer(Player player, OfflinePlayer target) {
        ArrayList<UUID> ignores = userIgnores.computeIfAbsent(player.getUniqueId(), (uuid) -> new ArrayList<>());

        if (ignores.contains(target.getUniqueId())) {
            ignores.remove(target.getUniqueId());
            return false;
        } else {
            ignores.add(target.getUniqueId());
            return true;
        }
    }

    public boolean playerHasChannelMuted(Player player, ChatChannel chatChannel) {
        return getUserChannelMutes(player).contains(chatChannel.getName());
    }

    public boolean togglePlayerChannelMute(Player player, ChatChannel chatChannel) {
        ArrayList<String> toggles = getUserChannelMutes(player);

        if (toggles.contains(chatChannel.getName())) {
            toggles.remove(chatChannel.getName());
            return false;
        } else {
            toggles.add(chatChannel.getName());
            return true;
        }
    }

    public ArrayList<String> getUserChannelMutes(Player player) {
        return userToggles.computeIfAbsent(player.getUniqueId(), (uuid) -> new ArrayList<>());
    }

    public ArrayList<UUID> getShadowMutes() {
        return shadowMutes;
    }

    public boolean toggleShadowMute(OfflinePlayer player) {
        if (isUserShadowMuted(player)) {
            shadowMutes.remove(player.getUniqueId());
            return false;
        }

        shadowMutes.add(player.getUniqueId());
        return true;
    }

    public boolean isUserShadowMuted(OfflinePlayer player) {
        return shadowMutes.contains(player.getUniqueId());
    }

    public ChatChannel getPlayerChannel(Player player) {
        return userChannels.computeIfAbsent(player.getUniqueId(), (uuid) -> getDefaultChannel());
    }

    public void setPlayerChannel(Player player, ChatChannel chatChannel) {
        userChannels.put(player.getUniqueId(), chatChannel);
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

    public ArrayList<ChatChannel> getChannels() {
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
