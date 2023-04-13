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
package net.draycia.carbon.velocity.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.users.MojangProfileResolver;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public class VelocityProfileResolver implements ProfileResolver {

    private final ProxyServer proxyServer;
    private final MojangProfileResolver mojang;

    @Inject
    public VelocityProfileResolver(final ProxyServer proxyServer, final MojangProfileResolver mojang) {
        this.proxyServer = proxyServer;
        this.mojang = mojang;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        final var serverPlayer = this.proxyServer.getPlayer(username);

        return serverPlayer.map(player -> CompletableFuture.completedFuture(player.getUniqueId()))
            .orElseGet(() -> this.mojang.resolveUUID(username));
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        final var serverPlayer = this.proxyServer.getPlayer(uuid);

        return serverPlayer.map(player -> CompletableFuture.completedFuture(player.getUsername()))
            .orElseGet(() -> this.mojang.resolveName(uuid));
    }

    @Override
    public void shutdown() {
        this.mojang.shutdown();
    }

}
