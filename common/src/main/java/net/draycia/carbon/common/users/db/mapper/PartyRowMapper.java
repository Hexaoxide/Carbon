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

import com.google.common.base.Suppliers;
import com.google.inject.Injector;
import com.google.inject.Key;
import io.leangen.geantyref.TypeToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Supplier;
import net.draycia.carbon.common.users.PartyImpl;
import net.draycia.carbon.common.users.UserManagerInternal;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@DefaultQualifier(NonNull.class)
public final class PartyRowMapper implements RowMapper<PartyImpl> {

    private final Supplier<UserManagerInternal<?>> users;

    public PartyRowMapper(final Injector injector) {
        this.users = Suppliers.memoize(() -> (UserManagerInternal<?>) injector.getInstance(Key.get(new TypeToken<UserManagerInternal<?>>() {}.getType())));
    }

    @Override
    public PartyImpl map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        final ColumnMapper<UUID> uuid = ctx.findColumnMapperFor(UUID.class).orElseThrow();
        return PartyImpl.create(
            rs.getString("name"),
            uuid.map(rs, "partyid", ctx),
            this.users.get()
        );
    }

}
