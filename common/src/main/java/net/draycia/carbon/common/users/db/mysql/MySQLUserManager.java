/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.users.db.mysql;

import com.google.inject.Inject;
import com.google.inject.MembersInjector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.db.ComponentArgumentFactory;
import net.draycia.carbon.common.users.db.DBType;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.users.db.KeyArgumentFactory;
import net.draycia.carbon.common.users.db.QueriesLocator;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

// TODO: Dispatch updates using messaging system when users are modified
@DefaultQualifier(NonNull.class)
public final class MySQLUserManager extends DatabaseUserManager {

    private MySQLUserManager(
        final Jdbi jdbi,
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector
    ) {
        super(
            jdbi,
            new QueriesLocator(DBType.MYSQL),
            logger,
            profileResolver,
            playerInjector
        );
    }

    @Override
    protected CarbonPlayerCommon loadOrCreate(final UUID uuid) {
        return this.jdbi.withHandle(handle -> {
            final Optional<CarbonPlayerCommon> carbonPlayerCommon = handle.createQuery(this.locator.query("select-player"))
                .bind("id", uuid)
                .mapTo(CarbonPlayerCommon.class)
                .findOne();

            if (carbonPlayerCommon.isEmpty()) {
                // Player doesn't exist in the DB, create them!
                final String name = Objects.requireNonNull(this.profileResolver.resolveName(uuid).join());
                final CarbonPlayerCommon player = new CarbonPlayerCommon(name, uuid);

                this.bindPlayerArguments(handle.createUpdate(this.locator.query("insert-player")), player).execute();

                return player;
            }

            handle.createQuery(this.locator.query("select-ignores"))
                .bind("id", uuid)
                .mapTo(UUID.class)
                .forEach(ignoredPlayer -> carbonPlayerCommon.get().ignoredPlayers().add(ignoredPlayer));
            handle.createQuery(this.locator.query("select-leftchannels"))
                .bind("id", uuid)
                .mapTo(Key.class)
                .forEach(channel -> {
                    final @Nullable ChatChannel chatChannel = CarbonChatProvider.carbonChat()
                        .channelRegistry()
                        .get(channel);
                    if (chatChannel == null) {
                        return;
                    }
                    carbonPlayerCommon.get().leftChannels().add(channel);
                });
            return carbonPlayerCommon.get();
        });
    }

    @Override
    protected Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player) {
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

    public static final class Factory {

        private final DatabaseSettings databaseSettings;
        private final Logger logger;
        private final ProfileResolver profileResolver;
        private final MembersInjector<CarbonPlayerCommon> playerInjector;

        @Inject
        private Factory(
            final ConfigFactory configFactory,
            final Logger logger,
            final ProfileResolver profileResolver,
            final MembersInjector<CarbonPlayerCommon> playerInjector
        ) {
            this.databaseSettings = configFactory.primaryConfig().databaseSettings();
            this.logger = logger;
            this.profileResolver = profileResolver;
            this.playerInjector = playerInjector;
        }

        public MySQLUserManager create() {
            try {
                //Class.forName("org.postgresql.Driver");
                Class.forName("org.mariadb.jdbc.Driver");
                Class.forName("com.mysql.cj.jdbc.Driver"); // Manually loading this might not be necessary
            } catch (final Exception exception) {
                exception.printStackTrace();
            }

            final HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMaximumPoolSize(20);
            hikariConfig.setJdbcUrl(this.databaseSettings.url());
            hikariConfig.setUsername(this.databaseSettings.username());
            hikariConfig.setPassword(this.databaseSettings.password());
            hikariConfig.setThreadFactory(ConcurrentUtil.carbonThreadFactory(this.logger, "MySQLUserManagerHCP"));

            final DataSource dataSource = new HikariDataSource(hikariConfig);

            final Flyway flyway = Flyway.configure(CarbonChat.class.getClassLoader())
                .baselineVersion("0")
                .baselineOnMigrate(true)
                .locations("queries/migrations/mysql")
                .dataSource(dataSource)
                .validateOnMigrate(true)
                .load();

            flyway.repair();
            flyway.migrate();

            final Jdbi jdbi = Jdbi.create(dataSource)
                .registerArrayType(UUID.class, "uuid")
                .registerArgument(new ComponentArgumentFactory())
                .registerArgument(new KeyArgumentFactory())
                .registerArgument(new MySQLUUIDArgumentFactory())
                .registerRowMapper(new MySQLPlayerRowMapper())
                .installPlugin(new SqlObjectPlugin());

            return new MySQLUserManager(jdbi, this.logger, this.profileResolver, this.playerInjector);
        }

    }

}
