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
package net.draycia.carbon.common.users.db;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.CachingUserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.PartyImpl;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.db.argument.ComponentArgumentFactory;
import net.draycia.carbon.common.users.db.argument.KeyArgumentFactory;
import net.draycia.carbon.common.users.db.mapper.ComponentColumnMapper;
import net.draycia.carbon.common.users.db.mapper.KeyColumnMapper;
import net.draycia.carbon.common.users.db.mapper.PartyRowMapper;
import net.draycia.carbon.common.users.db.mapper.PlayerRowMapper;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.SQLDrivers;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@DefaultQualifier(NonNull.class)
public final class DatabaseUserManager extends CachingUserManager {

    private final Jdbi jdbi;
    private final QueriesLocator locator;
    private final ChannelRegistry channelRegistry;
    private final HikariDataSource dataSource;

    private DatabaseUserManager(
        final Jdbi jdbi,
        final HikariDataSource dataSource,
        final QueriesLocator locator,
        final Logger logger,
        final ProfileResolver profileResolver,
        final Injector injector,
        final Provider<MessagingManager> messagingManager,
        final PacketFactory packetFactory,
        final ChannelRegistry channelRegistry,
        final CarbonServer server
    ) {
        super(
            logger,
            profileResolver,
            injector,
            messagingManager,
            packetFactory,
            server
        );
        this.jdbi = jdbi;
        this.dataSource = dataSource;
        this.locator = locator;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public CarbonPlayerCommon loadOrCreate(final UUID uuid) {
        return this.jdbi.withHandle(handle -> {
            final @Nullable CarbonPlayerCommon carbonPlayerCommon = handle.createQuery(this.locator.query("select-player"))
                .bind("id", uuid)
                .mapTo(CarbonPlayerCommon.class)
                .findOne()
                .orElse(null);
            if (carbonPlayerCommon == null) {
                return new CarbonPlayerCommon(null, uuid);
            }

            handle.createQuery(this.locator.query("select-ignores"))
                .bind("id", uuid)
                .mapTo(UUID.class)
                .forEach(ignoredPlayer -> carbonPlayerCommon.ignoring(ignoredPlayer, true, true));
            handle.createQuery(this.locator.query("select-leftchannels"))
                .bind("id", uuid)
                .mapTo(Key.class)
                .forEach(channel -> {
                    final @Nullable ChatChannel chatChannel = this.channelRegistry.channel(channel);

                    if (chatChannel == null) {
                        return;
                    }

                    carbonPlayerCommon.leaveChannel(chatChannel, true);
                });
            return carbonPlayerCommon;
        });
    }

    @Override
    public void saveSync(final CarbonPlayerCommon player) {
        this.jdbi.useTransaction(handle -> {
            final int inserted = this.bindPlayerArguments(handle.createUpdate(this.locator.query("insert-player")), player).execute();
            if (inserted != 1) {
                this.bindPlayerArguments(handle.createUpdate(this.locator.query("update-player")), player).execute();
            }

            handle.createUpdate(this.locator.query("clear-ignores"))
                .bind("id", player.uuid())
                .execute();
            handle.createUpdate(this.locator.query("clear-leftchannels"))
                .bind("id", player.uuid())
                .execute();

            final Set<UUID> ignored = player.ignoring();
            if (!ignored.isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-ignores"));
                for (final UUID ignoredPlayer : ignored) {
                    batch.bind("id", player.uuid()).bind("ignoredplayer", ignoredPlayer).add();
                }
                batch.execute();
            }

            final List<Key> left = player.leftChannels();
            if (!left.isEmpty()) {
                final PreparedBatch batch = handle.prepareBatch(this.locator.query("save-leftchannels"));
                for (final Key leftChannel : left) {
                    batch.bind("id", player.uuid()).bind("channel", leftChannel).add();
                }
                batch.execute();
            }
        });
    }

    @Override
    protected @Nullable PartyImpl loadParty(final UUID uuid) {
        return this.jdbi.withHandle(handle -> {
            final @Nullable PartyImpl party = this.selectParty(handle, uuid);
            if (party == null) {
                return null;
            }

            final List<UUID> members = handle.createQuery(this.locator.query("select-party-members"))
                .bind("partyid", uuid)
                .mapTo(UUID.class)
                .list();

            party.rawMembers().addAll(members);

            return party;
        });
    }

    private @Nullable PartyImpl selectParty(final Handle handle, final UUID uuid) {
        return handle.createQuery(this.locator.query("select-party"))
            .bind("partyid", uuid)
            .mapTo(PartyImpl.class)
            .findOne()
            .orElse(null);
    }

    @Override
    protected void saveSync(final PartyImpl party, final Map<UUID, PartyImpl.ChangeType> changes) {
        this.jdbi.useTransaction(handle -> {
            final @Nullable PartyImpl existing = this.selectParty(handle, party.id());
            if (existing == null) {
                handle.createUpdate(this.locator.query("insert-party"))
                    .bind("partyid", party.id())
                    .bind("name", party.serializedName())
                    .execute();
            }

            @Nullable PreparedBatch add = null;
            @Nullable PreparedBatch remove = null;
            for (final Map.Entry<UUID, PartyImpl.ChangeType> entry : changes.entrySet()) {
                final UUID id = entry.getKey();
                final PartyImpl.ChangeType type = entry.getValue();
                switch (type) {
                    case ADD -> {
                        if (add == null) {
                            add = handle.prepareBatch(this.locator.query("insert-party-member"));
                        }
                        add.bind("partyid", party.id()).bind("playerid", id).add();
                    }
                    case REMOVE -> {
                        if (remove == null) {
                            remove = handle.prepareBatch(this.locator.query("drop-party-member"));
                        }
                        remove.bind("playerid", id).add();
                    }
                }
            }
            if (add != null) {
                add.execute();
            }
            if (remove != null) {
                remove.execute();
            }
        });
    }

    @Override
    public void disbandSync(final UUID id) {
        this.jdbi.useHandle(handle -> {
            handle.createUpdate(this.locator.query("drop-party")).bind("partyid", id).execute();
            handle.createUpdate(this.locator.query("clear-party-members")).bind("partyid", id).execute();
        });
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.dataSource.close();
    }

    private Update bindPlayerArguments(final Update update, final CarbonPlayerCommon player) {
        final @Nullable Component nickname = player.nicknameRaw();
        @Nullable String nicknameJson = GsonComponentSerializer.gson().serializeOrNull(nickname);
        if (nicknameJson != null && nicknameJson.toCharArray().length > 8192) {
            this.logger.error("Serialized nickname for player {} was too long ({}>8192), it cannot be saved: {}", player.uuid(), nicknameJson.length(), nicknameJson);
            nicknameJson = null;
        }
        return update.bind("id", player.uuid())
            .bind("muted", player.muted())
            .bind("deafened", player.deafened())
            .bind("selectedchannel", player.selectedChannelKey())
            .bind("displayname", nicknameJson)
            .bind("lastwhispertarget", player.lastWhisperTarget())
            .bind("whisperreplytarget", player.whisperReplyTarget())
            .bind("spying", player.spying())
            .bind("ignoringdms", player.ignoringDirectMessages())
            .bind("party", player.partyId());
    }

    public static final class Factory {

        private final ChannelRegistry channelRegistry;
        private final ConfigManager configManager;
        private final Logger logger;
        private final ProfileResolver profileResolver;
        private final Injector injector;
        private final Provider<MessagingManager> messagingManager;
        private final PacketFactory packetFactory;
        private final CarbonServer server;

        @Inject
        private Factory(
            final ChannelRegistry channelRegistry,
            final ConfigManager configManager,
            final Logger logger,
            final ProfileResolver profileResolver,
            final Injector injector,
            final Provider<MessagingManager> messagingManager,
            final PacketFactory packetFactory,
            final CarbonServer server
        ) {
            this.channelRegistry = channelRegistry;
            this.configManager = configManager;
            this.logger = logger;
            this.profileResolver = profileResolver;
            this.injector = injector;
            this.messagingManager = messagingManager;
            this.packetFactory = packetFactory;
            this.server = server;
        }

        public DatabaseUserManager create(final String migrationsLocation, final Consumer<Jdbi> configureJdbi) {
            return this.create(migrationsLocation, configureJdbi, this.configManager.primaryConfig().databaseSettings());
        }

        public DatabaseUserManager create(final String migrationsLocation, final Consumer<Jdbi> configureJdbi, final DatabaseSettings databaseSettings) {
            SQLDrivers.loadFrom(this.getClass().getClassLoader());

            final HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(databaseSettings.url());
            hikariConfig.setUsername(databaseSettings.username());
            hikariConfig.setPassword(databaseSettings.password());
            hikariConfig.setPoolName("CarbonChat-HikariPool");
            hikariConfig.setThreadFactory(ConcurrentUtil.carbonThreadFactory(this.logger, "HikariPool"));

            final DatabaseSettings.ConnectionPool cfg = Objects.requireNonNull(this.configManager.primaryConfig().databaseSettings().connectionPool());
            hikariConfig.setMaximumPoolSize(cfg.maximumPoolSize);
            hikariConfig.setMinimumIdle(cfg.minimumIdle);
            hikariConfig.setMaxLifetime(cfg.maximumLifetime);
            hikariConfig.setKeepaliveTime(cfg.keepaliveTime);
            hikariConfig.setConnectionTimeout(cfg.connectionTimeout);

            final HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            final Flyway flyway = Flyway.configure(CarbonChat.class.getClassLoader())
                .baselineVersion("0")
                .baselineOnMigrate(true)
                .locations(migrationsLocation)
                .dataSource(dataSource)
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .load();

            LogFactory.setLogCreator(new CarbonLogCreator(this.logger));
            this.logger.info("Executing Flyway database migrations...");
            flyway.repair();
            flyway.migrate();
            LogFactory.setLogCreator(null);

            final Jdbi jdbi = Jdbi.create(dataSource)
                .registerArgument(new ComponentArgumentFactory())
                .registerArgument(new KeyArgumentFactory())
                .registerRowMapper(CarbonPlayerCommon.class, new PlayerRowMapper())
                .registerRowMapper(PartyImpl.class, new PartyRowMapper())
                .registerColumnMapper(Key.class, new KeyColumnMapper())
                .registerColumnMapper(Component.class, new ComponentColumnMapper())
                .installPlugin(new SqlObjectPlugin());

            configureJdbi.accept(jdbi);

            return new DatabaseUserManager(
                jdbi,
                dataSource,
                new QueriesLocator(this.configManager.primaryConfig().storageType()),
                this.logger,
                this.profileResolver,
                this.injector,
                this.messagingManager,
                this.packetFactory,
                this.channelRegistry,
                this.server
            );
        }

    }

    private record CarbonLogCreator(Logger logger) implements LogCreator {

        @Override
        public Log createLogger(final Class<?> clazz) {
            final Logger l = this.logger;
            return new Log() {
                @Override
                public boolean isDebugEnabled() {
                    return true;
                }

                @Override
                public void debug(final String message) {
                    l.debug("  [{}] {}", clazz.getSimpleName(), message);
                }

                @Override
                public void info(final String message) {
                    l.info("  [{}] {}", clazz.getSimpleName(), message);
                }

                @Override
                public void warn(final String message) {
                    l.warn("  [{}] {}", clazz.getSimpleName(), message);
                }

                @Override
                public void error(final String message) {
                    l.error("  [{}] {}", clazz.getSimpleName(), message);
                }

                @Override
                public void error(final String message, final Exception e) {
                    l.error("  [{}] {}", clazz.getSimpleName(), message, e);
                }

                @Override
                public void notice(final String message) {
                    l.info("  [{}] (Notice) {}", clazz.getSimpleName(), message);
                }
            };
        }
    }

}
