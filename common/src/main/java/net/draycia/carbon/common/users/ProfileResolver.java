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
package net.draycia.carbon.common.users;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface ProfileResolver {

    CompletableFuture<@Nullable UUID> resolveUUID(String username, boolean cacheOnly);

    default CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return this.resolveUUID(username, false);
    }

    CompletableFuture<@Nullable String> resolveName(UUID uuid, boolean cacheOnly);

    default CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return this.resolveName(uuid, false);
    }

    void shutdown();

}
