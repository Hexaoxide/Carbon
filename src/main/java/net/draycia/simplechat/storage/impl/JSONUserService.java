package net.draycia.simplechat.storage.impl;

import com.google.common.cache.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class JSONUserService extends UserService {

    private final SimpleChat simpleChat;

    private final LoadingCache<UUID, SimpleChatUser> userCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(this::saveUser)
            .build(CacheLoader.from(this::loadUser));

    private Gson gson;
    private Type userType = new TypeToken<SimpleChatUser>() {}.getType();

    public JSONUserService(SimpleChat simpleChat) {
        this.simpleChat =simpleChat;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(simpleChat, userCache::cleanUp, 0L, 20 * 60 * 10);

        gson = new GsonBuilder().setPrettyPrinting().create();
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
    public void cleanUp() {
        userCache.invalidateAll();
        userCache.cleanUp();
    }

    private SimpleChatUser loadUser(UUID uuid) {
        File userFile = new File(simpleChat.getDataFolder(), "users/" + uuid.toString() + ".json");
        ensureFileExists(userFile);

        try (JsonReader reader = gson.newJsonReader(new FileReader(userFile))) {
            SimpleChatUser user = gson.fromJson(reader, userType);

            if (user != null) {
                return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SimpleChatUser(uuid);
    }

    private void saveUser(RemovalNotification<UUID, SimpleChatUser> notification) {
        File userFile = new File(simpleChat.getDataFolder(), "users/" + notification.getKey().toString() + ".json");
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
