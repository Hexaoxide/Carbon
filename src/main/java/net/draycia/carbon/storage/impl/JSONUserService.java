package net.draycia.carbon.storage.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class JSONUserService implements UserService {

    @NonNull
    private final CarbonChat carbonChat;
    @NonNull
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @NonNull
    private final Type userType = new TypeToken<CarbonChatUser>() {}.getType();
    @NonNull
    private final LoadingCache<@NonNull UUID, @NonNull CarbonChatUser> userCache = CacheBuilder.newBuilder()
            .removalListener(this::saveUser)
            .build(CacheLoader.from(this::loadUser));

    public JSONUserService(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(carbonChat, userCache::cleanUp, 0L, 20 * 60 * 10);
    }

    @Override
    public void onDisable() {
        userCache.invalidateAll();
        userCache.cleanUp();
    }

    @Override
    @Nullable
    public ChatUser wrap(@NonNull String name) {
        Player player = Bukkit.getPlayer(name);

        if (player != null) {
            return wrap(player);
        }

        return wrap(Bukkit.getOfflinePlayer(name));
    }

    @Override
    @Nullable
    public ChatUser wrap(@NonNull OfflinePlayer player) {
        return wrap(player.getUniqueId());
    }

    @Override
    @Nullable
    public ChatUser wrap(@NonNull UUID uuid) {
        try {
            return userCache.get(uuid);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Nullable
    public ChatUser wrapIfLoaded(@NonNull UUID uuid) {
        return userCache.getIfPresent(uuid);
    }

    @Override
    @Nullable
    public ChatUser refreshUser(@NonNull UUID uuid) {
        userCache.invalidate(uuid);

        return this.wrap(uuid);
    }

    @Override
    public void invalidate(@NonNull ChatUser user) {
        userCache.invalidate(user.getUUID());
    }

    @Override
    public void validate(@NonNull ChatUser user) {
        userCache.put(user.getUUID(), (CarbonChatUser) user);
    }

    @NonNull
    private CarbonChatUser loadUser(@NonNull UUID uuid) {
        File userFile = new File(carbonChat.getDataFolder(), "users/" + uuid.toString() + ".json");
        ensureFileExists(userFile);

        try (JsonReader reader = gson.newJsonReader(new FileReader(userFile))) {
            CarbonChatUser user = gson.fromJson(reader, userType);

            if (user != null) {
                return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new CarbonChatUser(uuid);
    }

    private void saveUser(@NonNull RemovalNotification<@NonNull UUID, @NonNull CarbonChatUser> notification) {
        File userFile = new File(carbonChat.getDataFolder(), "users/" + notification.getKey().toString() + ".json");
        ensureFileExists(userFile);

        try (JsonWriter writer = gson.newJsonWriter(new FileWriter(userFile))) {
            gson.toJson(notification.getValue(), userType, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFileExists(@NonNull File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
