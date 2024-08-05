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
package net.draycia.carbon.common.channels;

import net.draycia.carbon.api.channels.ChannelPermissionResult;
import net.draycia.carbon.api.channels.ChannelPermissions;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.messages.CarbonMessages;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

public record ChannelPermissionsImpl(String permission, CarbonMessages messages) implements ChannelPermissions {
    @Override
    public ChannelPermissionResult joinPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            player.hasPermission(this.permission()),
            () -> this.messages.channelNoPermission(player)
        );
    }

    @Override
    public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            player.hasPermission(this.permission() + ".speak"),
            () -> this.messages.channelNoPermission(player)
        );
    }

    @Override
    public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
        return channelPermissionResult(
            player.hasPermission(this.permission() + ".see"),
            () -> this.messages.channelNoPermission(player)
        );
    }

    @Override
    public boolean dynamic() {
        return false;
    }
}
