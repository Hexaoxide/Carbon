package net.draycia.carbon.storage.impl;

import com.google.common.cache.*;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JSONUserService implements UserService {

    private final CarbonChat carbonChat;

    private final LoadingCache<UUID, CarbonChatUser> userCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(this::saveUser)
            .build(CacheLoader.from(this::loadUser));

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type userType = new TypeToken<CarbonChatUser>() {}.getType();

    public JSONUserService(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(carbonChat, userCache::cleanUp, 0L, 20 * 60 * 10);
    }

    @Override
    public ChatUser wrap(OfflinePlayer player) {
        return wrap(player.getUniqueId());
    }

    @Override
    public ChatUser wrap(UUID uuid) {
        try {
            return userCache.get(uuid);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Nullable
    public ChatUser wrapIfLoaded(UUID uuid) {
        return userCache.getIfPresent(uuid);
    }

    @Override
    public void refreshUser(UUID uuid) {
        userCache.invalidate(uuid);
    }

    @Override
    public void cleanUp() {
        userCache.invalidateAll();
        userCache.cleanUp();
    }

    private CarbonChatUser loadUser(UUID uuid) {
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

    private void saveUser(RemovalNotification<UUID, CarbonChatUser> notification) {
        File userFile = new File(carbonChat.getDataFolder(), "users/" + notification.getKey().toString() + ".json");
        ensureFileExists(userFile);

        try (JsonWriter writer = gson.newJsonWriter(new FileWriter(userFile))) {
            gson.toJson(notification.getValue(), userType, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureFileExists(File file) {
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
