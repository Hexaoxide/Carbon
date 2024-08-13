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
import java.util.ArrayList;
import java.util.List;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.messages.CarbonMessages;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class RadiusListener implements Listener {

    @Inject
    public RadiusListener(
        final CarbonEventHandler events,
        final CarbonMessages carbonMessages
    ) {
        events.subscribe(CarbonChatEvent.class, 0, false, event -> {
            if (event.chatChannel() == null) {
                return;
            }

            final double radius = event.chatChannel().radius();

            if (radius < 0) {
                return;
            }

            final List<CarbonPlayer> spyingPlayers = new ArrayList<>();

            if (radius == 0) {
                event.recipients().removeIf(audience -> {
                    if (audience.equals(event.sender())) {
                        return false;
                    }

                    if (audience instanceof CarbonPlayer carbonPlayer) {
                        final boolean sameWorld = carbonPlayer.sameWorldAs(event.sender());

                        if (!sameWorld && carbonPlayer.spying()) {
                            spyingPlayers.add(carbonPlayer);
                        }

                        return !sameWorld;
                    }

                    return false;
                });
            } else {
                event.recipients().removeIf(audience -> {
                    if (audience.equals(event.sender())) {
                        return false;
                    }

                    if (audience instanceof CarbonPlayer carbonPlayer) {
                        if (!event.sender().sameWorldAs(carbonPlayer)) {
                            if (carbonPlayer.spying()) {
                                spyingPlayers.add(carbonPlayer);
                            }
                            return true;
                        }

                        final double distance = carbonPlayer.distanceSquaredFrom(event.sender());
                        final boolean outOfRange = distance > (radius * radius);

                        if (outOfRange && carbonPlayer.spying()) {
                            spyingPlayers.add(carbonPlayer);
                        }

                        return outOfRange;
                    }

                    return false;
                });
            }
            if (event.recipients().size() <= 2 && event.chatChannel().emptyRadiusRecipientsMessage()) { // the player and cosole
                carbonMessages.emptyRecipients(event.sender());
                return;
            }

            for (final CarbonPlayer player : spyingPlayers) {
                carbonMessages.radiusSpy(player, event.message());
            }
        });
    }

}
