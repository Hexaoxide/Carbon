package net.draycia.carbon.common.users;

import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.UserChannelSettings;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DbRow;
import co.aikar.idb.DbStatement;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserService;
import net.draycia.carbon.api.config.SQLCredentials;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

public class MySQLUserService<T extends ChatUser> implements UserService<T> {

  private final @NonNull CarbonChat carbonChat;
  private final @NonNull Database database;
  private final @NonNull Supplier<@NonNull Iterable<@NonNull T>> supplier;
  private final @NonNull Function<UUID, T> userFactory;
  private final @NonNull Function<String, UUID> nameResolver;

  private final @NonNull LoadingCache<@NonNull UUID, @NonNull T> userCache = CacheBuilder.newBuilder()
    .removalListener(this::saveUser)
    .build(CacheLoader.from(this::loadUser));

  public MySQLUserService(final @NonNull CarbonChat carbonChat, final @NonNull SQLCredentials credentials,
                          final @NonNull Supplier<@NonNull Iterable<@NonNull T>> supplier,
                          final @NonNull Function<UUID, T> userFactory,
                          final @NonNull Function<String, UUID> nameResolver) {
    this.carbonChat = carbonChat;
    this.supplier = supplier;
    this.userFactory = userFactory;
    this.nameResolver = nameResolver;

    final String username = credentials.username();
    final String password = credentials.password();
    final String database = credentials.password();
    final String host = credentials.host();
    final int port = credentials.port();

    final String hostAndPort = host + ":" + port;

    final DatabaseOptions options = DatabaseOptions.builder().mysql(username, password, database, hostAndPort).build();
    this.database = PooledDatabaseOptions.builder().options(options).createHikariDatabase();

    DB.setGlobalDatabase(this.database);

    try {
      this.database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_users (uuid CHAR(36) PRIMARY KEY," +
        "channel VARCHAR(16), muted BOOLEAN, shadowmuted BOOLEAN, spyingwhispers BOOLEAN," +
        "nickname VARCHAR(512))");

      // Ignore the exception, it's just saying the column already exists
      try {
        this.database.executeUpdate("ALTER TABLE sc_users ADD COLUMN spyingwhispers BOOLEAN DEFAULT false");
        this.database.executeUpdate("ALTER TABLE sc_users ADD COLUMN nickname VARCHAR(512) DEFAULT false");
      } catch (final SQLSyntaxErrorException ignored) {
      }

      this.database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_channel_settings (uuid CHAR(36), channel CHAR(16), spying BOOLEAN, ignored BOOLEAN, color TINYTEXT, PRIMARY KEY (uuid, channel))");

      this.database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_ignored_users (uuid CHAR(36), user CHAR(36), PRIMARY KEY (uuid, user))");
    } catch (final SQLException exception) {
      exception.printStackTrace();
    }

    final TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        MySQLUserService.this.userCache.cleanUp();
      }
    };

    new Timer().schedule(timerTask, 0L, 300000L);
  }

  @Override
  public UUID resolve(final @NonNull String name) {
    return this.nameResolver.apply(name);
  }

  @Override
  public void onDisable() {
    this.userCache.invalidateAll();
    this.userCache.cleanUp();
    this.database.close();
  }

  @Override
  @Nullable
  public T wrap(final @NonNull UUID uuid) {
    try {
      return this.userCache.get(uuid);
    } catch (final ExecutionException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  @Override
  @Nullable
  public T wrapIfLoaded(final @NonNull UUID uuid) {
    return this.userCache.getIfPresent(uuid);
  }

  @Override
  @Nullable
  public T refreshUser(final @NonNull UUID uuid) {
    this.userCache.invalidate(uuid);

    return this.wrap(uuid);
  }

  @Override
  public void invalidate(final @NonNull T user) {
    this.userCache.invalidate(user.uuid());
  }

  @Override
  public void validate(final @NonNull T user) {
    this.userCache.put(user.uuid(), user);
  }

  @Override
  public @NonNull Iterable<@NonNull T> onlineUsers() {
    return this.supplier.get();
  }

  private @Nullable T loadUser(final @NonNull UUID uuid) {
    final T user = this.userFactory.apply(uuid);

    try (final DbStatement statement = this.database.query("SELECT * from sc_users WHERE uuid = ?;")) {
      statement.execute(uuid.toString());

      final DbRow users = statement.getNextRow();

      if (users == null) {
        return user;
      }

      final List<DbRow> channelSettings = this.database.getResults("SELECT * from sc_channel_settings WHERE uuid = ?;", uuid.toString());
      final List<DbRow> ignoredUsers = this.database.getResults("SELECT * from sc_ignored_users WHERE uuid = ?;", uuid.toString());

      final ChatChannel channel = this.carbonChat.channelRegistry().channelOrDefault(users.getString("channel"));

      if (channel != null) {
        user.selectedChannel(channel, true);
      }

      final String nickname = users.getString("nickname");

      if (nickname != null) {
        user.nickname(nickname, true);
      }

      user.muted(users.<Boolean>get("muted"), true);
      user.shadowMuted(users.<Boolean>get("shadowmuted"), true);
      user.spyingWhispers(users.<Boolean>get("spyingwhispers"), true);

      for (final DbRow channelSetting : channelSettings) {
        final ChatChannel chatChannel = this.carbonChat.channelRegistry().get(channelSetting.getString("channel"));

        if (chatChannel != null) {
          final UserChannelSettings settings = user.channelSettings(chatChannel);

          settings.spying(channelSetting.<Boolean>get("spying"), true);
          settings.ignoring(channelSetting.<Boolean>get("ignored"), true);

          final String color = channelSetting.getString("color");

          if (color != null) {
            settings.color(TextColor.fromHexString(color), true);
          }
        }
      }

      for (final DbRow ignoredUser : ignoredUsers) {
        user.ignoringUser(UUID.fromString(ignoredUser.getString("user")), true, true);
      }
    } catch (final SQLException exception) {
      exception.printStackTrace();
    }

    return user;
  }

  private void saveUser(final @NonNull RemovalNotification<@NonNull UUID, @NonNull T> notification) {
    final T user = notification.getValue();

    this.database.createTransaction(stm -> {
      // Save user general data
      String selectedName = null;

      if (user.selectedChannel() != null) {
        selectedName = user.selectedChannel().key();
      }

      this.carbonChat.logger().info("Saving user data!");
      stm.executeUpdateQuery("INSERT INTO sc_users (uuid, channel, muted, shadowmuted, spyingwhispers, nickname) VALUES (?, ?, ?, ?, ?, ?) " +
          "ON DUPLICATE KEY UPDATE channel = ?, muted = ?, shadowmuted = ?, spyingwhispers = ?, nickname =?",
        user.uuid().toString(), selectedName, user.muted(), user.shadowMuted(), user.spyingWhispers(), user.nickname(),
        selectedName, user.muted(), user.shadowMuted(), user.spyingWhispers(), user.nickname());

      this.carbonChat.logger().info("Saving user channel settings!");
      // Save user channel settings
      for (final Map.Entry<String, ? extends UserChannelSettings> entry : user.channelSettings().entrySet()) {
        final UserChannelSettings value = entry.getValue();

        String colorString = null;

        if (value.color() != null) {
          colorString = value.color().asHexString();
        }

        stm.executeUpdateQuery("INSERT INTO sc_channel_settings (uuid, channel, spying, ignored, color) VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE spying = ?, ignored = ?, color = ?",
          user.uuid().toString(), entry.getKey(), value.spying(), value.ignored(), colorString,
          value.spying(), value.ignored(), colorString);
      }

      this.carbonChat.logger().info("Saving user ignores!");
      // Save user ignore list (remove old entries then add new ones)
      // TODO: keep DB up to date with settings as settings are mutated
      stm.executeUpdateQuery("DELETE FROM sc_ignored_users WHERE uuid = ?", user.uuid().toString());

      for (final UUID entry : user.ignoredUsers()) {
        stm.executeUpdateQuery("INSERT INTO sc_ignored_users (uuid, user) VALUES (?, ?)",
          user.uuid().toString(), entry.toString());
      }

      return true;
    });
  }

}
