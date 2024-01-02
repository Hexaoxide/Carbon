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
package net.draycia.carbon.paper.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.users.MojangProfileResolver;
import net.draycia.carbon.common.users.ProfileResolver;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class PaperProfileResolver implements ProfileResolver {

    private final Server server;
    private final ProfileResolver mojang;

    @Inject
    private PaperProfileResolver(final Server server, final MojangProfileResolver mojang) {
        this.server = server;
        this.mojang = mojang;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        final @Nullable Player online = this.server.getPlayer(username);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getUniqueId());
        }

        return this.mojang.resolveUUID(username, cacheOnly);
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        final @Nullable Player online = this.server.getPlayer(uuid);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getName());
        }

        return this.mojang.resolveName(uuid, cacheOnly);
    }

    @Override
    public void shutdown() {
        this.mojang.shutdown();
    }

}
