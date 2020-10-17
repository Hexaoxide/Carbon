package net.draycia.carbon.common.users;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.ConsoleUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class JSONUserService<T extends PlayerUser, C extends ConsoleUser> implements UserService<T> {

  private @NonNull final CarbonChat carbonChat;
  private @NonNull final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private @NonNull final Type userType;
  private @NonNull final Supplier<@NonNull Iterable<@NonNull T>> supplier;
  private @NonNull final Function<UUID, T> userFactory;
  private @NonNull final Function<String, UUID> nameResolver;
  private @NonNull final Supplier<C> consoleFactory;

  private @NonNull final LoadingCache<@NonNull UUID, @NonNull T> userCache = CacheBuilder.newBuilder()
    .removalListener(this::saveUser)
    .build(CacheLoader.from(this::loadUser));

  public JSONUserService(@NonNull final Class<? extends CarbonUser> userType,
                         @NonNull final CarbonChat carbonChat,
                         @NonNull final Supplier<@NonNull Iterable<@NonNull T>> supplier,
                         @NonNull final Function<UUID, T> userFactory,
                         @NonNull final Function<String, UUID> nameResolver,
                         @NonNull final Supplier<C> consoleFactory) {
    this.userType = userType;
    this.carbonChat = carbonChat;
    this.supplier = supplier;
    this.userFactory = userFactory;
    this.nameResolver = nameResolver;
    this.consoleFactory = consoleFactory;

    final TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        JSONUserService.this.userCache.cleanUp();
      }
    };

    new Timer().schedule(timerTask, 300000L, 300000L);
  }

  @Override
  public UUID resolve(@NonNull final String name) {
    return this.nameResolver.apply(name);
  }

  @Override
  public void onDisable() {
    this.userCache.invalidateAll();
    this.userCache.cleanUp();
  }

  @Override
  public @Nullable T wrap(@NonNull final UUID uuid) {
    try {
      return this.userCache.get(uuid);
    } catch (final ExecutionException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  @Override
  public @Nullable C consoleUser() {
    return this.consoleFactory.get();
  }

  @Override
  public @Nullable T wrapIfLoaded(@NonNull final UUID uuid) {
    return this.userCache.getIfPresent(uuid);
  }

  @Override
  public @Nullable T refreshUser(@NonNull final UUID uuid) {
    this.userCache.invalidate(uuid);

    return this.wrap(uuid);
  }

  @Override
  public void invalidate(@NonNull final T user) {
    this.userCache.invalidate(user.uuid());
  }

  @Override
  public void validate(@NonNull final T user) {
    this.userCache.put(user.uuid(), user);
  }

  @Override
  public @NonNull Iterable<@NonNull T> onlineUsers() {
    return this.supplier.get();
  }

  private @NonNull T loadUser(@NonNull final UUID uuid) {
    final File userFile = new File(this.carbonChat.dataFolder().toFile(), "users/" + uuid.toString() + ".json");
    this.ensureFileExists(userFile);

    try (final JsonReader reader = this.gson.newJsonReader(new FileReader(userFile))) {
      final T user = this.gson.fromJson(reader, this.userType);

      if (user != null) {
        return user;
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
    }

    return this.userFactory.apply(uuid);
  }

  private void saveUser(@NonNull final RemovalNotification<@NonNull UUID, @NonNull T> notification) {
    final File userFile = new File(this.carbonChat.dataFolder().toFile(), "users/" + notification.getKey().toString() + ".json");
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
