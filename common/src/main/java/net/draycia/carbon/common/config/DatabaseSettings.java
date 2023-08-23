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
package net.draycia.carbon.common.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@DefaultQualifier(Nullable.class)
@ConfigSerializable
public class DatabaseSettings {

    public DatabaseSettings() {
    }

    public DatabaseSettings(final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Comment("""
        JDBC URL. Suggested defaults for each DB:
        MySQL: jdbc:mysql://host:3306/DB
        MariaDB: jdbc:mariadb://host:3306/DB
        PostgreSQL: jdbc:postgresql://host:5432/database
        """)
    private String url = "jdbc:mysql://localhost:3306/carbon";

    @Comment("The connection username.")
    private String username = "username";

    @Comment("The connection password.")
    private String password = "password";

    public String url() {
        return this.url;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

}
