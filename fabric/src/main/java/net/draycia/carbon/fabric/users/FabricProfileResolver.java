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
package net.draycia.carbon.fabric.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.users.MojangProfileResolver;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class FabricProfileResolver implements ProfileResolver {

    private final CarbonChatFabric carbonChatFabric;
    private final ProfileResolver mojang;

    @Inject
    private FabricProfileResolver(final CarbonChatFabric carbonChatFabric, final MojangProfileResolver mojang) {
        this.carbonChatFabric = carbonChatFabric;
        this.mojang = mojang;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        final @Nullable ServerPlayer online = this.carbonChatFabric.minecraftServer().getPlayerList().getPlayerByName(username);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getUUID());
        }

        return this.mojang.resolveUUID(username, cacheOnly);
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        final @Nullable ServerPlayer online = this.carbonChatFabric.minecraftServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getGameProfile().getName());
        }

        return this.mojang.resolveName(uuid, cacheOnly);
    }

    @Override
    public void shutdown() {
        this.mojang.shutdown();
    }

}
