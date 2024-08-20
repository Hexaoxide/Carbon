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
package net.draycia.carbon.api.channels;

import java.util.function.Function;
import net.draycia.carbon.api.users.CarbonPlayer;

/**
 * Permissions handling for a channel.
 *
 * @since 3.0.0
 */
public interface ChannelPermissions {

    /**
     * Checks if the player may join this channel.
     *
     * @param carbonPlayer the player attempting to join
     * @return if the player may join
     * @since 3.0.0
     */
    ChannelPermissionResult joinPermitted(CarbonPlayer carbonPlayer);

    /**
     * Checks if the player may send messages in this channel.
     *
     * @param carbonPlayer the player attempting to speak
     * @return if the player may speak
     * @since 3.0.0
     */
    ChannelPermissionResult speechPermitted(CarbonPlayer carbonPlayer);

    /**
     * Checks if the player may receive messages from this channel.
     *
     * @param player the player that's receiving messages
     * @return if the player may receive messages
     * @since 3.0.0
     */
    ChannelPermissionResult hearingPermitted(CarbonPlayer player);

    /**
     * Returns whether the result of {@link #joinPermitted(CarbonPlayer)} is dynamic.
     *
     * <p>An example of a dynamic permissions is the built-in party channel that only allows players in a party to join.</p>
     *
     * <p>An example of static permissions is the built-in config channels that simply check permission strings. The fact that a player's
     * permissions may change during gameplay does not make the permission dynamic, as the server will resend commands on permission changes.</p>
     *
     * <p>If the result is static, then we can avoid sending commands to the player that they will just get denied use
     * of on execute. If it's dynamic, we must send the command regardless in case they gain permission later.</p>
     *
     * @return whether the permissions are dynamic
     * @since 3.0.0
     */
    boolean dynamic();

    /**
     * Creates a new {@link ChannelPermissions} that performs the same check for
     * {@link #joinPermitted(CarbonPlayer)}, {@link #hearingPermitted(CarbonPlayer)},
     * and {@link #speechPermitted(CarbonPlayer)}.
     *
     * @param check permission check
     * @return new permissions object
     * @since 3.0.0
     */
    static ChannelPermissions uniformDynamic(final Function<CarbonPlayer, ChannelPermissionResult> check) {
        return new ChannelPermissions() {
            @Override
            public ChannelPermissionResult joinPermitted(final CarbonPlayer player) {
                return check.apply(player);
            }

            @Override
            public ChannelPermissionResult speechPermitted(final CarbonPlayer player) {
                return check.apply(player);
            }

            @Override
            public ChannelPermissionResult hearingPermitted(final CarbonPlayer player) {
                return check.apply(player);
            }

            @Override
            public boolean dynamic() {
                return true;
            }
        };
    }

}
