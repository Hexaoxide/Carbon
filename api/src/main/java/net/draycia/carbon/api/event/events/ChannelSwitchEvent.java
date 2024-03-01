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
package net.draycia.carbon.api.event.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.Cancellable;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Called when a player switches channels.
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface ChannelSwitchEvent extends CarbonEvent, Cancellable {

    /**
     * The player switching channels.
     *
     * @since 3.0.0
     */
    CarbonPlayer player();

    /**
     * The channel the player is switching to.
     *
     * @since 3.0.0
     */
    ChatChannel channel();

    /**
     * Sets the player's new channel.
     *
     * @param chatChannel the new channel
     * @since 3.0.0
     */
    void channel(final ChatChannel chatChannel);

}
