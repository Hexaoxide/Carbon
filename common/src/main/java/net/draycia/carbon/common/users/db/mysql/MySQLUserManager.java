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
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.db.ComponentArgumentFactory;
import net.draycia.carbon.common.users.db.DBType;
import net.draycia.carbon.common.users.db.DatabaseUserManager;
import net.draycia.carbon.common.users.db.KeyArgumentFactory;
import net.draycia.carbon.common.users.db.KeyColumnMapper;
import net.draycia.carbon.common.users.db.QueriesLocator;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.SQLDrivers;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@DefaultQualifier(NonNull.class)
public final class MySQLUserManager extends DatabaseUserManager {

    private MySQLUserManager(
        final Jdbi jdbi,
        final Logger logger,
        final ProfileResolver profileResolver,
        final MembersInjector<CarbonPlayerCommon> playerInjector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final ChannelRegistry channelRegistry
    ) {
        super(
            jdbi,
            new QueriesLocator(DBType.MYSQL),
            logger,
            profileResolver,
            playerInjector,
            messagingManager,
            packetFactory,
            channelRegistry
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
                return new CarbonPlayerCommon(null, uuid);
            }

            handle.createQuery(this.locator.query("select-ignores"))
                .bind("id", uuid)
                .mapTo(UUID.class)
                .forEach(ignoredPlayer -> carbonPlayerCommon.get().ignoring(ignoredPlayer, true, true));
            handle.createQuery(this.locator.query("select-leftchannels"))
                .bind("id", uuid)
                .mapTo(Key.class)
                .forEach(channel -> {
                    final @Nullable ChatChannel chatChannel = this.channelRegistry.channel(channel);

                    if (chatChannel == null) {
                        return;
                    }

                    carbonPlayerCommon.get().leaveChannel(chatChannel, true);
                });
            return carbonPlayerCommon.get();
        });
    }

    public static final class Factory {

        private final DatabaseSettings databaseSettings;
        private final Logger logger;
        private final ProfileResolver profileResolver;
        private final MembersInjector<CarbonPlayerCommon> playerInjector;
        private final Provider<MessagingManager> messagingManager;
        private final PacketFactory packetFactory;
        private final ChannelRegistry channelRegistry;

        @Inject
        private Factory(
            final ChannelRegistry channelRegistry,
            final ConfigManager configManager,
            final Logger logger,
            final ProfileResolver profileResolver,
            final MembersInjector<CarbonPlayerCommon> playerInjector,
            final Provider<MessagingManager> messagingManager,
            final PacketFactory packetFactory
        ) {
            this.channelRegistry = channelRegistry;
            this.databaseSettings = configManager.primaryConfig().databaseSettings();
            this.logger = logger;
            this.profileResolver = profileResolver;
            this.playerInjector = playerInjector;
            this.messagingManager = messagingManager;
            this.packetFactory = packetFactory;
        }

        public MySQLUserManager create() {
            SQLDrivers.loadFrom(this.getClass().getClassLoader());

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
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .load();

            flyway.repair();
            flyway.migrate();

            final Jdbi jdbi = Jdbi.create(dataSource)
                .registerArgument(new ComponentArgumentFactory())
                .registerArgument(new KeyArgumentFactory())
                .registerArgument(new MySQLUUIDArgumentFactory())
                .registerArrayType(UUID.class, "uuid")
                .registerColumnMapper(new KeyColumnMapper())
                .registerRowMapper(new MySQLPlayerRowMapper())
                .installPlugin(new SqlObjectPlugin());

            return new MySQLUserManager(jdbi, this.logger, this.profileResolver, this.playerInjector, this.messagingManager, this.packetFactory, this.channelRegistry);
        }

    }

}
