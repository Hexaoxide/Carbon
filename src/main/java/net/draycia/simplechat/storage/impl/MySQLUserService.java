package net.draycia.simplechat.storage.impl;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.Database;
import co.aikar.idb.DbRow;
import co.aikar.idb.DbStatement;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MySQLUserService extends UserService {

    private final LoadingCache<UUID, SimpleChatUser> userCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(this::saveUser)
            .build(CacheLoader.from(this::loadUser));

    private SimpleChat simpleChat;
    private Database database;

    private MySQLUserService() { }

    public MySQLUserService(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        ConfigurationSection section = simpleChat.getConfig().getConfigurationSection("storage");

        String username = section.getString("username");
        String password = section.getString("password");
        String dbname = section.getString("database");
        String hostandport = section.getString("host") + ":" + section.getString("port");

        database = BukkitDB.createHikariDatabase(simpleChat, username, password, dbname, hostandport);

        try {
            database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_users (uuid CHAR(36) PRIMARY KEY," +
                    "channel TINYTEXT , muted BOOLEAN, shadowmuted BOOLEAN)");

            database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_ignored_channels (uuid CHAR(36), channel TINYTEXT)");

            database.executeUpdate("CREATE TABLE IF NOT EXISTS sc_ignored_users (uuid CHAR(36), user CHAR(36))");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(simpleChat, userCache::cleanUp, 0L, 20 * 60 * 10);
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
        SimpleChatUser user = new SimpleChatUser(uuid);


        try (DbStatement statement = database.query("SELECT * from sc_users WHERE uuid = ?;")) {
            statement.execute(uuid.toString());

            DbRow users = statement.getNextRow();

            if (users == null) {
                return user;
            }

            List<DbRow> ignoredChannels = database.getResults("SELECT * from sc_ignored_channels WHERE uuid = ?;", uuid.toString());
            List<DbRow> ignoredUsers = database.getResults("SELECT * from sc_ignored_users WHERE uuid = ?;", uuid.toString());

            user.setSelectedChannel(simpleChat.getChannel(users.getString("channel")));
            user.setMuted(users.<Boolean>get("muted"));
            user.setShadowMuted(users.<Boolean>get("shadowmuted"));

            for (DbRow ignoredChannel : ignoredChannels) {
                user.setIgnoringChannel(simpleChat.getChannel(ignoredChannel.getString("channel")), true);
            }

            for (DbRow ignoredUser : ignoredUsers) {
                user.setIgnoringUser(UUID.fromString(ignoredUser.getString("user")), true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    private void saveUser(RemovalNotification<UUID, SimpleChatUser> notification) {
        SimpleChatUser user = notification.getValue();

        try {
            database.executeUpdate("SET @uuid = ?, @channel = ?, @muted = ?, @shadowmuted = ? " +
                    "INSERT INTO db_users (uuid, channel, muted, shadowmuted) VALUES (@uuid, @channel, @muted, @shadowmuted) " +
                    "ON DUPLICATE KEY UPDATE uuid = @uuid, channel = @channel, muted = @muted, shadowmuted = @shadowmuted;",
                    user.getUUID().toString(), user.getSelectedChannel().getName(), user.isMuted(), user.isShadowMuted());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
