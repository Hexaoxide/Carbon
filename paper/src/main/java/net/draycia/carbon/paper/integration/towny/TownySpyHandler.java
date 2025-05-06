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
package net.draycia.carbon.paper.integration.towny;

import com.google.inject.Inject;
import com.palmergames.bukkit.towny.object.ResidentList;
import com.palmergames.bukkit.towny.object.TownyObject;
import java.util.List;
import java.util.UUID;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.listeners.Listener;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class TownySpyHandler implements Listener {

    private final CarbonEventHandler events;
    private final CarbonMessages messages;
    private final CarbonServer server;

    @Inject
    public TownySpyHandler(final CarbonEventHandler events, final CarbonMessages messages, final CarbonServer server) {
        this.events = events;
        this.messages = messages;
        this.server = server;
    }

    public void register() {
        this.events.subscribe(CarbonChatEvent.class, 100, false, event -> {
            final CarbonPlayer sender = event.sender();

            switch (event.chatChannel()) {
                case TownChannel townChannel -> this.handleSpy(sender, event.message(), townChannel);
                case NationChannel nationChannel -> this.handleSpy(sender, event.message(), nationChannel);
                default -> {
                }
            }
        });
    }

    private <T extends ResidentList> void handleSpy(
        final CarbonPlayer sender,
        final Component message,
        final ResidentListChannel<T> residentListChannel
    ) {
        final @Nullable T residentList = residentListChannel.residentList(sender);

        if (residentList == null) {
            return;
        }

        final List<UUID> recipients = residentListChannel.onlinePlayers(residentList).stream()
            .map(Player::getUniqueId)
            .toList();
        final Component townyName = this.residentListName(residentList);

        for (final CarbonPlayer player : this.server.players()) {
            if (player.spying() && !recipients.contains(player.uuid())) {
                this.messages.townySpy(player, sender.uuid(), sender.displayName(), sender.username(), message, townyName);
            }
        }
    }

    private Component residentListName(final ResidentList residentList) {
        if (residentList instanceof TownyObject townyObject) {
            return Component.text(townyObject.getName());
        }

        return Component.text("Unknown");
    }

}
