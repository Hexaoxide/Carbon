/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.common.channels.PartyChatChannel;
import net.draycia.carbon.common.config.ConfigManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PartyPingHandler implements Listener {

    @Inject
    public PartyPingHandler(final CarbonEventHandler events, final ConfigManager configManager) {
        events.subscribe(CarbonChatEvent.class, 100, false, event -> {
            if (!(event.chatChannel() instanceof PartyChatChannel)) {
                return;
            }

            final @Nullable Sound sound = configManager.primaryConfig().partyChat().messageSound;

            if (configManager.primaryConfig().partyChat().playSound && sound != null) {
                for (final Audience recipient : event.recipients()) {
                    // Don't ping the message sender
                    if (event.sender().uuid().equals(recipient.get(Identity.UUID).orElse(null))) {
                        continue;
                    }

                    recipient.playSound(sound);
                }
            }
        });
    }

}
