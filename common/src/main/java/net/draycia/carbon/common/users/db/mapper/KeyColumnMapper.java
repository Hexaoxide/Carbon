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
package net.draycia.carbon.common.users.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.draycia.carbon.common.util.Strings;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Subst;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class KeyColumnMapper implements ColumnMapper<Key> {

    @Override
    public Key map(final ResultSet rs, final int columnNumber, final StatementContext ctx) throws SQLException {
        final @Nullable @Subst("key:value") String keyValue = Strings.trim(rs.getString(columnNumber));

        if (keyValue != null) {
            return Key.key(keyValue);
        }

        return null;
    }

}
