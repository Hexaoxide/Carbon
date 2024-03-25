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
import java.util.Set;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.channels.PartyChatChannel;
import net.draycia.carbon.common.messages.CarbonMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PartyChatSpyHandler implements Listener {

    @Inject
    public PartyChatSpyHandler(final CarbonEventHandler events, final CarbonMessages messages, final CarbonServer server) {
        events.subscribe(CarbonChatEvent.class, 100, false, event -> {
            if (!(event.chatChannel() instanceof PartyChatChannel)) {
                return;
            }

            final @Nullable Party party = event.sender().party().get();
            final Set<UUID> members = party == null ? Set.of() : party.members();

            for (final CarbonPlayer player : server.players()) {
                if (player.spying() && !members.contains(player.uuid())) {
                    messages.partySpy(player, event.message());
                }
            }
        });
    }

}
