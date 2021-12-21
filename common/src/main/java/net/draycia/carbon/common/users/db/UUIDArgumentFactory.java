/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josh Taylor (broccolai)
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

import java.sql.Types;
import java.util.UUID;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

// https://github.com/broccolai/tickets/blob/55a23b5fcfdc8e4b4bfa861ea7ec620506bd0dfa/core/src/main/java/broccolai/tickets/core/storage/factory/UUIDArgumentFactory.java
public final class UUIDArgumentFactory extends AbstractArgumentFactory<UUID> {

    public UUIDArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    public Argument build(final UUID value, final ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setString(position, value.toString());
    }

}
