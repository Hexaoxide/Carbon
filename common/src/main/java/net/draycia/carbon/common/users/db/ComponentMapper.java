package net.draycia.carbon.common.users.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ComponentMapper implements ColumnMapper<Component> {

    final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

    @Override
    public Component map(final ResultSet r, final int columnNumber, final StatementContext ctx) throws SQLException {
        return this.serializer.deserialize(r.getString(columnNumber));
    }

    @Override
    public Component map(final ResultSet r, final String columnLabel, final StatementContext ctx) throws SQLException {
        return this.serializer.deserialize(r.getString(columnLabel));
    }

}
