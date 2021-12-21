package net.draycia.carbon.common.users.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.DatabaseSettings;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class MariaDBUserManager implements UserManager<CarbonPlayerCommon> {

    private final Logger logger;
    private final Jdbi jdbi;

    private final Map<UUID, CarbonPlayerCommon> userCache = Collections.synchronizedMap(new HashMap<>());
    private final QueriesLocator locator = new QueriesLocator();

    private MariaDBUserManager(
        final Logger logger,
        final Jdbi jdbi
    ) {
        this.logger = logger;
        this.jdbi = jdbi;
    }

    public static @Nullable MariaDBUserManager manager(
        final Logger logger,
        final DatabaseSettings databaseSettings,
        final ClassLoader classLoader
    ) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(databaseSettings.url());
        hikariConfig.setUsername(databaseSettings.username());
        hikariConfig.setPassword(databaseSettings.password());

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        Flyway.configure(classLoader)
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

        return new MariaDBUserManager(logger, jdbi);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> carbonPlayer(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final @Nullable CarbonPlayerCommon cachedPlayer = this.userCache.get(uuid);

            if (cachedPlayer != null) {
                return new ComponentPlayerResult<>(cachedPlayer, empty());
            }

            return this.jdbi.withHandle(handle -> {
                final CarbonPlayerCommon carbonPlayerCommon = handle.createQuery(this.locator.query("select-player"))
                    .bind("uuid", uuid)
                    .mapTo(CarbonPlayerCommon.class)
                    .first();

                handle.createQuery(this.locator.query("select-ignores"))
                    .bind("uuid", uuid)
                    .mapTo(UUID.class)
                    .forEach(ignoredPlayer -> carbonPlayerCommon.ignoring(ignoredPlayer, true));

                return new ComponentPlayerResult<>(carbonPlayerCommon, empty());
            });
        }).completeOnTimeout(new ComponentPlayerResult<>(null, text("Timed out loading data of UUID [" + uuid + " ]")), 30, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(final CarbonPlayerCommon player) {

    }

    @Override
    public CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> saveAndInvalidatePlayer(final CarbonPlayerCommon player) {
        return this.savePlayer(player).thenApply(result -> {
            this.userCache.remove(player.uuid());

            return result;
        });
    }

}
