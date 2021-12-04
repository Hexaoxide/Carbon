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
package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.PlayerUtils;
import net.draycia.carbon.sponge.users.CarbonPlayerSponge;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

@DefaultQualifier(NonNull.class)
public class SpongePlayerJoinListener {

    private final CarbonChat carbonChat;
    private final ConfigFactory configFactory;
    private final UserManager<CarbonPlayerCommon> userManager;

    @Inject
    public SpongePlayerJoinListener(
        final CarbonChat carbonChat,
        final ConfigFactory configFactory,
        final UserManager<CarbonPlayerCommon> userManager
    ) {
        this.carbonChat = carbonChat;
        this.configFactory = configFactory;
        this.userManager = userManager;
    }

    @Listener
    public void onPlayerLogin(final ServerSideConnectionEvent.Join event) {
        if (!this.configFactory.primaryConfig().hideMutedJoinLeaveQuit()) {
            return;
        }

        final ComponentPlayerResult<CarbonPlayer> result = this.carbonChat.server().player(event.player().uniqueId()).join();
        final @Nullable CarbonPlayer player = result.player();

        if (player == null) {
            return;
        }

        // Don't show join messages when muted
        if (player.muted()) {
            event.setMessageCancelled(true);
        }
    }

    @Listener
    public void onPlayerQuit(final ServerSideConnectionEvent.Disconnect event) {
        final ComponentPlayerResult<CarbonPlayer> result =
            this.carbonChat.server().player(event.player().uniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer player = result.player();

        if (this.configFactory.primaryConfig().hideMutedJoinLeaveQuit()) {
            if (player.muted()) {
                event.setAudience(Audience.empty());
            }
        }

        PlayerUtils.saveAndInvalidatePlayer(this.carbonChat.server(), this.userManager, (CarbonPlayerSponge) player);
    }

}
