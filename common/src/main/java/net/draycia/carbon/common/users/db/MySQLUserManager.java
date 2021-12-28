/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.users.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.SaveOnChange;
import net.draycia.carbon.common.users.db.mysql.CarbonPlayerCommonRowMapper;
import net.draycia.carbon.common.users.db.mysql.ComponentArgumentFactory;
import net.draycia.carbon.common.users.db.mysql.KeyArgumentFactory;
import net.draycia.carbon.common.users.db.mysql.UUIDArgumentFactory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

// TODO: Dispatch updates using messaging system when users are modified
@DefaultQualifier(NonNull.class)
public final class MySQLUserManager implements UserManager<CarbonPlayerCommon>, SaveOnChange {

    private final Jdbi jdbi;

    private final Map<UUID, CarbonPlayerCommon> userCache = Collections.synchronizedMap(new HashMap<>());
    private final QueriesLocator locator = new QueriesLocator(DBType.MYSQL);

    private MySQLUserManager(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public static MySQLUserManager manager(
        final DatabaseSettings databaseSettings
    ) {
        try {
            //Class.forName("org.postgresql.Driver");
            Class.forName("org.mariadb.jdbc.Driver");
            Class.forName("com.mysql.jdbc.Driver");
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setJdbcUrl(databaseSettings.url());
        hikariConfig.setUsername(databaseSettings.username());
        hikariConfig.setPassword(databaseSettings.password());

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        Flyway.configure(CarbonChat.class.getClassLoader())
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .locations("queries/migrations/mysql")
            .dataSource(dataSource)
            .validateOnMigrate(true)
            .load()
            .migrate();

        final Jdbi jdbi = Jdbi.create(dataSource)
            .registerArrayType(UUID.class, "uuid")
            .registerArgument(new ComponentArgumentFactory())
            .registerArgument(new KeyArgumentFactory())
            .registerArgument(new UUIDArgumentFactory())
            .registerRowMapper(new CarbonPlayerCommonRowMapper())
            .installPlugin(new SqlObjectPlugin());

        return new MySQLUserManager(jdbi);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

            if (cachedPlayer != null) {
                return new ComponentPlayerResult<>(cachedPlayer, empty());
            }

            // TODO: players don't always exist
            return this.jdbi.withHandle(handle -> {
                try {
                    final @Nullable CarbonPlayerCommon carbonPlayerCommon = handle.createQuery(this.locator.query("select-player"))
                        .bind("id", uuid)
                        .mapTo(CarbonPlayerCommon.class)
                        .first();

                    if (carbonPlayerCommon != null) {
                        handle.createQuery(this.locator.query("select-ignores"))
                            .bind("id", uuid)
                            .mapTo(UUID.class)
                            .forEach(ignoredPlayer -> carbonPlayerCommon.ignoring(ignoredPlayer, true));
                    }

                    return new ComponentPlayerResult<>(carbonPlayerCommon, empty());
                } catch (final IllegalStateException exception) {
                    // Player doesn't exist in the DB, create them!
                    final String name = Objects.requireNonNull(
                        CarbonChatProvider.carbonChat().server().resolveName(uuid).join());

                    final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

                    this.bindPlayerArguments(handle.createUpdate(this.locator.query("insert-player")), player)
                        .execute();

                    return new ComponentPlayerResult<CarbonPlayerCommon>(null, text(""));
                }
            });
        }).completeOnTimeout(new ComponentPlayerResult<>(null, text("Timed out loading data of UUID [" + uuid + " ]")), 30, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(final CarbonPlayerCommon player) {
        return CompletableFuture.supplyAsync(() -> {
            return this.jdbi.withHandle(handle -> {
                this.bindPlayerArguments(handle.createUpdate(this.locator.query("save-player")), player)
                    .execute();

                if (!player.ignoredPlayers().isEmpty()) {
                    final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-ignores"));

                    for (final UUID ignoredPlayer : player.ignoredPlayers()) {
                        batch.bind("id", player.uuid()).bind("ignoredplayer", ignoredPlayer).add();
                    }

                    batch.execute();
                }

                // TODO: save ignoredplayers
                return new ComponentPlayerResult<>(player, empty());
            });
        });
    }

    private Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player) {
        return update
            .bind("id", player.uuid())
            .bind("muted", player.muted())
            .bind("deafened", player.deafened())
            .bind("selectedchannel", player.selectedChannelKey())
            .bind("username", player.username())
            .bind("displayname", player.displayName())
            .bind("lastwhispertarget", player.lastWhisperTarget())
            .bind("whisperreplytarget", player.whisperReplyTarget())
            .bind("spying", player.spying());
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> saveAndInvalidatePlayer(final CarbonPlayerCommon player) {
        return this.savePlayer(player).thenApply(result -> {
            this.userCache.remove(player.uuid());

            return result;
        });
    }

    @Override
    public int saveDisplayName(final UUID id, final @Nullable Component displayName) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveDisplayName(id, displayName));
    }

    @Override
    public int saveMuted(UUID id, boolean muted) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveMuted(id, muted));
    }

    @Override
    public int saveDeafened(UUID id, boolean deafened) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveDeafened(id, deafened));
    }

    @Override
    public int saveSpying(UUID id, boolean spying) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveSpying(id, spying));
    }

    @Override
    public int saveSelectedChannel(UUID id, @Nullable Key selectedChannel) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveSelectedChannel(id, selectedChannel));
    }

    @Override
    public int saveLastWhisperTarget(UUID id, @Nullable UUID lastWhisperTarget) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveLastWhisperTarget(id, lastWhisperTarget));
    }

    @Override
    public int saveWhisperReplyTarget(UUID id, @Nullable UUID whisperReplyTarget) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.saveWhisperReplyTarget(id, whisperReplyTarget));
    }

    @Override
    public int addIgnore(UUID id, UUID ignoredPlayer) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.addIgnore(id, ignoredPlayer));
    }

    @Override
    public int removeIgnore(UUID id, UUID ignoredPlayer) {
        return this.jdbi.withExtension(SQLSaveOnChange.class, changeSaver -> changeSaver.removeIgnore(id, ignoredPlayer));
    }

}
