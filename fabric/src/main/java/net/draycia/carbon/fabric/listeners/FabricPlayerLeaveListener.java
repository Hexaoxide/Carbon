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
package net.draycia.carbon.fabric.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricPlayerLeaveListener implements PlayerStatusMessageEvents.MessageEventListener {

    private final CarbonChat carbonChat;
    private final PrimaryConfig primaryConfig;
    private final UserManager<CarbonPlayerCommon> userManager;

    @Inject
    public FabricPlayerLeaveListener(
        final CarbonChat carbonChat,
        final PrimaryConfig primaryConfig,
        final UserManager<CarbonPlayerCommon> userManager
    ) {
        this.carbonChat = carbonChat;
        this.primaryConfig = primaryConfig;
        this.userManager = userManager;
    }

    @Override
    public void onMessage(final PlayerStatusMessageEvents.MessageEvent event) {
        final ComponentPlayerResult<CarbonPlayer> result =
            this.carbonChat.server().player(event.player().getUUID()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer player = result.player();

        if (this.primaryConfig.hideMutedJoinLeaveQuit()) {
            if (!player.muteEntries().isEmpty()) {
                event.disableMessage();
            }
        }

        PlayerUtils.saveAndInvalidatePlayer(this.carbonChat.server(), this.userManager, (CarbonPlayerFabric) player);
    }

}
