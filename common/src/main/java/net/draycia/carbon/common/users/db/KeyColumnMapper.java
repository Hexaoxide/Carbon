package net.draycia.carbon.common.users.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class KeyColumnMapper implements ColumnMapper<Key> {

    @Override
    public Key map(final ResultSet rs, final int columnNumber, final StatementContext ctx) throws SQLException {
        final @Subst("key:value") String keyValue = rs.getString(columnNumber);

        if (keyValue != null) {
            return Key.key(keyValue);
        }

        return null;
    }

}
