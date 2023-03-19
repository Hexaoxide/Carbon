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
package net.draycia.carbon.common.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PlayerUtils {

    private PlayerUtils() {
    }

    public static List<CompletableFuture<Void>> saveLoggedInPlayers(
        final CarbonServer carbonServer,
        final UserManager<CarbonPlayerCommon> userManager,
        final Logger logger
    ) {
        return carbonServer.players().stream()
            .map(player -> savePlayer(userManager, (WrappedCarbonPlayer) player, logger))
            .toList();
    }

    public static CompletableFuture<Void> savePlayer(
        final UserManager<CarbonPlayerCommon> userManager,
        final WrappedCarbonPlayer player,
        final Logger logger
    ) {
        final var saveResult =
            ((UserManagerInternal<CarbonPlayerCommon>) userManager).save(player.carbonPlayerCommon());
        saveResult.exceptionally(thr -> {
            logger.warn("Exception saving data for player {} with UUID {}", player.username(), player.uuid(), thr);
            return null;
        });
        return saveResult;
    }

}
