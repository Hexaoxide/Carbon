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
package net.draycia.carbon.api.event.events;

import net.draycia.carbon.api.event.Cancellable;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Called whenever a player privately messages another player.
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonPrivateChatEvent extends CarbonEvent, Cancellable {

    /**
     * Sets the message that will be sent.
     *
     * @param message the new message
     * @throws NullPointerException if message is null
     * @since 3.0.0
     */
    void message(Component message);

    /**
     * The message that will be sent.
     *
     * @return the message
     * @since 3.0.0
     */
    Component message();

    /**
     * The message sender.
     *
     * @return the sender of the message
     * @since 3.0.0
     */
    CarbonPlayer sender();

    /**
     * The message recipient.
     *
     * @return the recipient of the message
     * @since 3.0.0
     */
    CarbonPlayer recipient();

}
