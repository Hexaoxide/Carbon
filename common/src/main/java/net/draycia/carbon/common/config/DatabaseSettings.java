/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.config;

import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@NullMarked
@ConfigSerializable
public class DatabaseSettings {

    public DatabaseSettings() {
    }

    public DatabaseSettings(final @Nullable String url, final @Nullable String username, final @Nullable String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Comment("""
        JDBC URL. Suggested defaults for each DB:
        MySQL: jdbc:mysql://host:3306/DB
        MariaDB: jdbc:mariadb://host:3306/DB
        PostgreSQL: jdbc:postgresql://host:5432/database""")
    private @Nullable String url = "jdbc:mysql://localhost:3306/carbon";

    @Comment("The connection username.")
    private @Nullable String username = "username";

    @Comment("The connection password.")
    private @Nullable String password = "password";

    @Comment("Settings for the connection pool. This is an advanced configuration that most users won't need to touch.")
    private @Nullable ConnectionPool connectionPool = new ConnectionPool();

    public @Nullable String url() {
        return this.url;
    }

    public @Nullable String url(final @Nullable String url) {
        return this.url = url;
    }

    public @Nullable String username() {
        return this.username;
    }

    public @Nullable String password() {
        return this.password;
    }

    public @Nullable ConnectionPool connectionPool() {
        return this.connectionPool;
    }

    @ConfigSerializable
    public static class ConnectionPool {
        public int maximumPoolSize = 8;
        public int minimumIdle = 8;
        public long maximumLifetime = TimeUnit.MINUTES.toMillis(30);
        public long keepaliveTime = 0L;
        public long connectionTimeout = TimeUnit.SECONDS.toMillis(30);
    }

}
