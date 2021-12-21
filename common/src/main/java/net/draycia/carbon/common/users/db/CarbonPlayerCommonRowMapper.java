package net.draycia.carbon.common.users.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class CarbonPlayerCommonRowMapper implements RowMapper<CarbonPlayerCommon> {

    @Override
    public CarbonPlayerCommon map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new CarbonPlayerCommon(
            rs.getBoolean("muted"),
            rs.getBoolean("deafened"),
            rs.getObject("selectedchannel", Key.class),
            rs.getString("username"),
            rs.getObject("uuid", UUID.class),
            rs.getObject("displayname", Component.class),
            rs.getObject("lastwhispertarget", UUID.class),
            rs.getObject("whisperreplytarget", UUID.class),
            rs.getBoolean("spying")
        );
    }

}
