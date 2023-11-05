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
package net.draycia.carbon.paper.hooks.towny;

import com.google.inject.Inject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
abstract class ResidentListChannel extends ConfigChatChannel {

    protected final static TownyAPI TOWNY_API = TownyAPI.getInstance();

    private transient @MonotonicNonNull
    @Inject CarbonServer server;

    protected abstract @Nullable ResidentList residentList(final CarbonPlayer player);

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return this.residentList(player) != null
            ? ChannelPermissionResult.allowed()
            : ChannelPermissionResult.denied(Component.empty());
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return this.residentList(player) != null
            ? ChannelPermissionResult.allowed()
            : ChannelPermissionResult.denied(Component.empty());
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        @Nullable final ResidentList residentList = TOWNY_API.getTown(sender.uuid());

        if (residentList == null) {
            if (sender.online()) {
                sender.sendMessage(Component.text("You must join a town to use this channel.", NamedTextColor.RED));
            }

            return Collections.emptyList();
        }

        final List<Player> onlinePlayersInResidentList = TOWNY_API.getOnlinePlayers(residentList);

        final List<Audience> recipients = new ArrayList<>();

        recipients.addAll(onlinePlayersInResidentList);
        recipients.add(this.server.console());

        return recipients;
    }

}
