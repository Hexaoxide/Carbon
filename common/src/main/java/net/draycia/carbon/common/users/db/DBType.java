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

public final class DBType {

    private final String basePath;

    private DBType(final String basePath) {
        this.basePath = basePath;
    }

    public String basePath() {
        return this.basePath;
    }

    public static final DBType MYSQL = new DBType("queries/mysql/");
    public static final DBType POSTGRESQL = new DBType("queries/postgresql/");

}
