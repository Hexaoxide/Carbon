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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Subst;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class MySQLPlayerRowMapper implements RowMapper<CarbonPlayerCommon> {

    @Override
    public CarbonPlayerCommon map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final String id = rs.getString("id");
        final @Nullable String lastWhisperTarget = rs.getString("lastwhispertarget");
        final @Nullable String whisperReplyTarget = rs.getString("whisperreplytarget");
        final @Nullable @Subst("carbon:global") String selectedChannel = rs.getString("selectedchannel");
        final @Nullable String displayName = rs.getString("displayname");

        return new CarbonPlayerCommon(
            rs.getBoolean("muted"),
            rs.getBoolean("deafened"),
            selectedChannel == null ? null : Key.key(selectedChannel),
            rs.getString("username"),
            UUID.fromString(id),
            displayName == null ? null : GsonComponentSerializer.gson().deserialize(displayName),
            lastWhisperTarget == null ? null : UUID.fromString(lastWhisperTarget),
            whisperReplyTarget == null ? null : UUID.fromString(whisperReplyTarget),
            rs.getBoolean("spying")
        );
    }

}
