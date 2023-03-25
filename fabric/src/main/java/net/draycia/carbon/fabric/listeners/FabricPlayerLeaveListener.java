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
package net.draycia.carbon.fabric.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.fabric.FabricUserManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricPlayerLeaveListener implements ServerPlayConnectionEvents.Disconnect {

    private final FabricUserManager userManager;
    private final Logger logger;

    @Inject
    public FabricPlayerLeaveListener(
        final Logger logger,
        final FabricUserManager userManager
    ) {
        this.logger = logger;
        this.userManager = userManager;
    }

    @Override
    public void onPlayDisconnect(final ServerGamePacketListenerImpl handler, final MinecraftServer server) {
        this.userManager.loggedOut(handler.getPlayer().getGameProfile().getId()).exceptionally(thr -> {
            this.logger.warn("Exception saving data for player " + handler.getPlayer().getGameProfile().getName() + " with uuid " + handler.getPlayer().getGameProfile().getId());
            return null;
        });
    }

}
