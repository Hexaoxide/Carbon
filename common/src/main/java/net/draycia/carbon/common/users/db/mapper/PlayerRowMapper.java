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
package net.draycia.carbon.common.users.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@DefaultQualifier(NonNull.class)
public final class PlayerRowMapper implements RowMapper<CarbonPlayerCommon> {

    @Override
    public CarbonPlayerCommon map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final ColumnMapper<UUID> uuid = ctx.findColumnMapperFor(UUID.class).orElseThrow();
        return new CarbonPlayerCommon(
            rs.getBoolean("muted"),
            rs.getBoolean("deafened"),
            ctx.findColumnMapperFor(Key.class).orElseThrow().map(rs, "selectedchannel", ctx),
            null,
            uuid.map(rs, "id", ctx),
            ctx.findColumnMapperFor(Component.class).orElseThrow().map(rs, "displayname", ctx),
            uuid.map(rs, "lastwhispertarget", ctx),
            uuid.map(rs, "whisperreplytarget", ctx),
            rs.getBoolean("spying"),
            rs.getBoolean("ignoringdms"),
            uuid.map(rs, "party", ctx)
        );
    }

}
