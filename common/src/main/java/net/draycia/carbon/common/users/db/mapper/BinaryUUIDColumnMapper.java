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
import java.util.UUID;
import net.draycia.carbon.common.util.FastUuidSansHyphens;
import net.draycia.carbon.common.util.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class BinaryUUIDColumnMapper implements ColumnMapper<UUID> {

    @Override
    public UUID map(final ResultSet rs, final int columnNumber, final StatementContext ctx) throws SQLException {
        final byte @Nullable [] bytes = rs.getBytes(columnNumber);

        if (bytes != null) {
            return FastUuidSansHyphens.parseUuid(Strings.asHexString(bytes));
        }

        return null;
    }

}
