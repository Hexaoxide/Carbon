package net.draycia.carbon.common.users.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Update;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

// TODO: Dispatch updates using messaging system when users are modified
@DefaultQualifier(NonNull.class)
public class MariaDBUserManager implements UserManager<CarbonPlayerCommon> {

    private final Jdbi jdbi;

    private final Map<UUID, CarbonPlayerCommon> userCache = Collections.synchronizedMap(new HashMap<>());
    private final QueriesLocator locator = new QueriesLocator(DBType.MYSQL);

    private MariaDBUserManager(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public static MariaDBUserManager manager(
        final DatabaseSettings databaseSettings
    ) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(databaseSettings.url());
        hikariConfig.setUsername(databaseSettings.username());
        hikariConfig.setPassword(databaseSettings.password());

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        Flyway.configure(CarbonChatProvider.carbonChat().getClass().getClassLoader())
            .baselineVersion("0")
            .baselineOnMigrate(true)
            .locations("queries/migrations")
            .dataSource(dataSource)
            .validateOnMigrate(true)
            .load()
            .migrate();

        final Jdbi jdbi = Jdbi.create(dataSource)
            .registerArrayType(UUID.class, "uuid")
            .registerArgument(new UUIDArgumentFactory())
            .registerColumnMapper(Component.class, new ComponentMapper())
            .registerRowMapper(new CarbonPlayerCommonRowMapper());

        return new MariaDBUserManager(jdbi);
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

}
