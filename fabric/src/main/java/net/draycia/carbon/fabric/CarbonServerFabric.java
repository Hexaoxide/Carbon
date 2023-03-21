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
package net.draycia.carbon.fabric;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerFabric implements CarbonServer, ForwardingAudience.Single {

    private final CarbonChatFabric carbonChatFabric;
    private final UserManager<CarbonPlayerFabric> userManager;

    @Inject
    private CarbonServerFabric(final CarbonChatFabric carbonChatFabric, final UserManager<CarbonPlayerCommon> userManager) {
        this.carbonChatFabric = carbonChatFabric;
        this.userManager = new FabricUserManager(userManager, carbonChatFabric);
    }

    @Override
    public @NotNull Audience audience() {
        return FabricServerAudiences.of(this.carbonChatFabric.minecraftServer()).all();
    }

    @Override
    public Audience console() {
        return this.carbonChatFabric.minecraftServer();
    }

    @Override
    public List<? extends CarbonPlayer> players() {
        return this.carbonChatFabric.minecraftServer().getPlayerList().getPlayers().stream()
            .map(serverPlayer -> this.userManager.user(serverPlayer.getUUID()).getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public UserManager<CarbonPlayerFabric> userManager() {
        return this.userManager;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        final @Nullable ServerPlayer serverPlayer = this.carbonChatFabric.minecraftServer().getPlayerList().getPlayerByName(username);
        if (serverPlayer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(serverPlayer.getUUID());
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        final @Nullable ServerPlayer serverPlayer = this.carbonChatFabric.minecraftServer().getPlayerList().getPlayer(uuid);
        if (serverPlayer == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(serverPlayer.getGameProfile().getName());
    }

}
