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
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerFabric implements CarbonServer, ForwardingAudience.Single {

    private final MinecraftServerHolder serverHolder;
    private final FabricUserManager userManager;

    @Inject
    private CarbonServerFabric(final MinecraftServerHolder serverHolder, final FabricUserManager userManager) {
        this.serverHolder = serverHolder;
        this.userManager = userManager;
    }

    @Override
    public @NotNull Audience audience() {
        return FabricServerAudiences.of(this.serverHolder.requireServer()).all();
    }

    @Override
    public Audience console() {
        return this.serverHolder.requireServer().createCommandSourceStack();
    }

    @Override
    public List<? extends CarbonPlayer> players() {
        return this.serverHolder.requireServer().getPlayerList().getPlayers().stream()
            .map(serverPlayer -> this.userManager.user(serverPlayer.getUUID()).getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public UserManager<CarbonPlayerFabric> userManager() {
        return this.userManager;
    }

}
