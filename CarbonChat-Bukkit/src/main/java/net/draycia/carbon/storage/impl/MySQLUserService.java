package net.draycia.carbon.storage.impl;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.UserChannelSettings;
import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DbRow;
import co.aikar.idb.DbStatement;
import co.aikar.idb.HikariPooledDatabase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserService;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MySQLUserService implements UserService {

  @NonNull
  private final CarbonChat carbonChat;
  @NonNull
  private final Database database;
  @NonNull
  private final LoadingCache<@NonNull UUID, @NonNull CarbonChatUser> userCache = CacheBuilder.newBuilder()
    .removalListener(this::saveUser)
    .build(CacheLoader.from(this::loadUser));

  public MySQLUserService(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    final ConfigurationSection section = this.carbonChat.getConfig().getConfigurationSection("storage");

    if (section == null) {
      throw new IllegalStateException("Missing Database Credentials!");
    }

    final String username = section.getString("username", "username");
    final String password = section.getString("password", "password");
    final String dbname = section.getString("database", "0");
    final String hostandport = section.getString("hostname", "hostname") + ":" + section.getString("port", "0");

    this.database = new HikariPooledDatabase(BukkitDB.getRecommendedOptions(carbonChat, username, password, dbname, hostandport));

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
  }

  @Override
  public void onDisable() {
    this.userCache.invalidateAll();
    this.userCache.cleanUp();
    this.database.close();
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
    final CarbonChatUser user = new CarbonChatUser(uuid);

    try (final DbStatement statement = this.database.query("SELECT * from sc_users WHERE uuid = ?;")) {
      statement.execute(uuid.toString());

      final DbRow users = statement.getNextRow();

      if (users == null) {
        return user;
      }

      final List<DbRow> channelSettings = this.database.getResults("SELECT * from sc_channel_settings WHERE uuid = ?;", uuid.toString());
      final List<DbRow> ignoredUsers = this.database.getResults("SELECT * from sc_ignored_users WHERE uuid = ?;", uuid.toString());

      final ChatChannel channel = this.carbonChat.channelManager().channelOrDefault(users.getString("channel"));

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
        final ChatChannel chatChannel = this.carbonChat.channelManager().registry().get(channelSetting.getString("channel"));

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

  private void saveUser(@NonNull final RemovalNotification<@NonNull UUID, @NonNull CarbonChatUser> notification) {
    final CarbonChatUser user = notification.getValue();

    this.database.createTransaction(stm -> {
      // Save user general data
      String selectedName = null;

      if (user.selectedChannel() != null) {
        selectedName = user.selectedChannel().key();
      }

      this.carbonChat.getLogger().info("Saving user data!");
      stm.executeUpdateQuery("INSERT INTO sc_users (uuid, channel, muted, shadowmuted, spyingwhispers, nickname) VALUES (?, ?, ?, ?, ?, ?) " +
          "ON DUPLICATE KEY UPDATE channel = ?, muted = ?, shadowmuted = ?, spyingwhispers = ?, nickname =?",
        user.uuid().toString(), selectedName, user.muted(), user.shadowMuted(), user.spyingwhispers(), user.nickname(),
        selectedName, user.muted(), user.shadowMuted(), user.spyingwhispers(), user.nickname());

      this.carbonChat.getLogger().info("Saving user channel settings!");
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

      this.carbonChat.getLogger().info("Saving user ignores!");
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
