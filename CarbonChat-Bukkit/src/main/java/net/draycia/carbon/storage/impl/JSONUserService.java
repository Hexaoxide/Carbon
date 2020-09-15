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
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserService;
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

  public JSONUserService(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    Bukkit.getScheduler().scheduleSyncRepeatingTask(carbonChat, this.userCache::cleanUp, 0L, 20 * 60 * 10);
  }

  @Override
  public void onDisable() {
    this.userCache.invalidateAll();
    this.userCache.cleanUp();
  }

  @Override
  @Nullable
  public ChatUser wrap(@NonNull final String name) {
    final Player player = Bukkit.getPlayer(name);

    if (player != null) {
      return this.wrap(player);
    }

    return this.wrap(Bukkit.getOfflinePlayer(name));
  }

  @Override
  @Nullable
  public ChatUser wrap(@NonNull final OfflinePlayer player) {
    return this.wrap(player.getUniqueId());
  }

  @Override
  @Nullable
  public ChatUser wrap(@NonNull final UUID uuid) {
    try {
      return this.userCache.get(uuid);
    } catch (final ExecutionException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  @Override
  @Nullable
  public ChatUser wrapIfLoaded(@NonNull final UUID uuid) {
    return this.userCache.getIfPresent(uuid);
  }

  @Override
  @Nullable
  public ChatUser refreshUser(@NonNull final UUID uuid) {
    this.userCache.invalidate(uuid);

    return this.wrap(uuid);
  }

  @Override
  public void invalidate(@NonNull final ChatUser user) {
    this.userCache.invalidate(user.uuid());
  }

  @Override
  public void validate(@NonNull final ChatUser user) {
    this.userCache.put(user.uuid(), (CarbonChatUser) user);
  }

  @NonNull
  private CarbonChatUser loadUser(@NonNull final UUID uuid) {
    final File userFile = new File(this.carbonChat.getDataFolder(), "users/" + uuid.toString() + ".json");
    this.ensureFileExists(userFile);

    try (final JsonReader reader = this.gson.newJsonReader(new FileReader(userFile))) {
      final CarbonChatUser user = this.gson.fromJson(reader, this.userType);

      if (user != null) {
        return user;
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
    }

    return new CarbonChatUser(uuid);
  }

  private void saveUser(@NonNull final RemovalNotification<@NonNull UUID, @NonNull CarbonChatUser> notification) {
    final File userFile = new File(this.carbonChat.getDataFolder(), "users/" + notification.getKey().toString() + ".json");
    this.ensureFileExists(userFile);

    try (final JsonWriter writer = this.gson.newJsonWriter(new FileWriter(userFile))) {
      this.gson.toJson(notification.getValue(), this.userType, writer);
    } catch (final IOException exception) {
      exception.printStackTrace();
    }
  }

  private void ensureFileExists(@NonNull final File file) {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (final IOException exception) {
        exception.printStackTrace();
      }
    }
  }

}
