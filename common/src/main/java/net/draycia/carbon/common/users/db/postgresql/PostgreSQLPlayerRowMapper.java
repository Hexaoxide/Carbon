package net.draycia.carbon.common.users.db.postgresql;

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

public class PostgreSQLPlayerRowMapper implements RowMapper<CarbonPlayerCommon> {

    @Override
    public CarbonPlayerCommon map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final @Nullable @Subst("carbon:global") String selectedChannel = rs.getString("selectedchannel");
        final @Nullable String displayName = rs.getString("displayname");

        return new CarbonPlayerCommon(
            rs.getBoolean("muted"),
            rs.getBoolean("deafened"),
            selectedChannel == null ? null : Key.key(selectedChannel),
            rs.getString("username"),
            rs.getObject("id", UUID.class),
            displayName == null ? null : GsonComponentSerializer.gson().deserialize(displayName),
            rs.getObject("lastwhispertarget", UUID.class),
            rs.getObject("whisperreplytarget", UUID.class),
            rs.getBoolean("spying")
        );
    }

}
