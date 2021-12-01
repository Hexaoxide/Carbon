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
import net.draycia.carbon.common.config.ConfigFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@DefaultQualifier(NonNull.class)
public class SpongePlayerDeathListener {

    private final CarbonChat carbonChat;
    private final ConfigFactory configFactory;

    @Inject
    public SpongePlayerDeathListener(final CarbonChat carbonChat, final ConfigFactory configFactory) {
        this.carbonChat = carbonChat;
        this.configFactory = configFactory;
    }

    @Listener
    public void onPlayerDeath(final DestructEntityEvent.Death event, final @First Player player) {
        // Early exit in case "hide muted join / leave messages when muted" is disabled
        if (!this.configFactory.primaryConfig().hideMutedJoinLeaveQuit()) {
            return;
        }

        final ComponentPlayerResult<CarbonPlayer> result = this.carbonChat.server().player(player.uniqueId()).join();

        if (result.player() == null) {
            return;
        }

        final CarbonPlayer carbonPlayer = result.player();

        if (!carbonPlayer.muteEntries().isEmpty()) {
            event.setMessageCancelled(true);
        }
    }

}
