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
package net.draycia.carbon.common.listeners;

import com.google.inject.Inject;
import java.util.stream.Collectors;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class RadiusListener {

    @Inject
    public RadiusListener(
        final CarbonChat carbonChat,
        final CarbonMessages carbonMessages
    ) {
        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, 0, false, event -> {
            if (event.chatChannel() == null) {
                return;
            }

            final double radius = event.chatChannel().radius();

            if (radius < 0) {
                return;
            }

            if (radius == 0) {
                event.recipients().removeIf(audience -> {
                    if (audience.equals(event.sender())) {
                        return false;
                    }

                    if (audience instanceof CarbonPlayer carbonPlayer) {
                        return !carbonPlayer.sameWorldAs(event.sender());
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
                            return true;
                        }

                        final double distance = carbonPlayer.distanceSquaredFrom(event.sender());
                        return distance > (radius * radius);
                    }

                    return false;
                });
            }
            if (event.recipients().size() > 1) return;

            carbonMessages.emptyRecipients(event.sender());
        });
    }

}
