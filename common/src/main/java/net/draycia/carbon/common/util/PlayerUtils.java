/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PlayerUtils {

    private PlayerUtils() {
    }

    public static List<CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>>> saveLoggedInPlayers(
        final CarbonServer carbonServer,
        final UserManager<CarbonPlayerCommon> userManager
    ) {
        return carbonServer.players().stream()
            .map(player -> savePlayer(carbonServer, userManager, (WrappedCarbonPlayer) player))
            .toList();
    }

    public static CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> savePlayer(
        final CarbonServer carbonServer,
        final UserManager<CarbonPlayerCommon> userManager,
        final WrappedCarbonPlayer player
    ) {
        final var saveResult =
            userManager.savePlayer(player.carbonPlayerCommon());

        saveResult.thenAccept(result -> {
            if (result.player() == null) {
                carbonServer.console().sendMessage(result.reason());
            }
        });

        saveResult.exceptionally(exception -> {
            exception.getCause().printStackTrace();
            return null;
        });

        return saveResult;
    }

    public static CompletableFuture<ComponentPlayerResult<CarbonPlayerCommon>> saveAndInvalidatePlayer(
        final CarbonServer carbonServer,
        final UserManager<CarbonPlayerCommon> userManager,
        final WrappedCarbonPlayer player
    ) {
        final var saveResult =
            userManager.saveAndInvalidatePlayer(player.carbonPlayerCommon());

        saveResult.thenAccept(result -> {
            if (result.player() == null) {
                carbonServer.console().sendMessage(result.reason());
            }
        });

        saveResult.exceptionally(exception -> {
            exception.getCause().printStackTrace();
            return null;
        });

        return saveResult;
    }

}
