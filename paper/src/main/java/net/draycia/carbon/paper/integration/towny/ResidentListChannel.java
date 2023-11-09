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
package net.draycia.carbon.paper.integration.towny;

import com.google.inject.Inject;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
abstract class ResidentListChannel<T extends ResidentList> extends ConfigChatChannel {

    protected final static TownyAPI TOWNY_API = TownyAPI.getInstance();

    protected transient @MonotonicNonNull @Inject CarbonMessages messages;

    protected abstract @Nullable T residentList(CarbonPlayer player);

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
        final @Nullable T residentList = this.residentList(sender);

        if (residentList == null) {
            if (sender.online()) {
                this.cannotUseChannel(sender);
            }

            return Collections.emptyList();
        }

        final List<Player> onlinePlayersInResidentList = this.onlinePlayers(residentList);

        final List<Audience> recipients = new ArrayList<>(onlinePlayersInResidentList);
        recipients.add(this.server.console());

        return recipients;
    }

    protected List<Player> onlinePlayers(final T residentList) {
        return TOWNY_API.getOnlinePlayers(residentList);
    }

    protected abstract void cannotUseChannel(CarbonPlayer player);

}
