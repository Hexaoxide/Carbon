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

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEvent;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * {@link CarbonEvent} that's called when channels are registered.
 *
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonChannelRegisterEvent extends CarbonEvent {

    /**
     * The channels that were registered.
     *
     * @return the registered channels
     * @since 2.1.0
     */
    Iterable<ChatChannel> channels();

    /**
     * Registers additional channels to the registry. Does not re-emit the event.
     *
     * @param key The key the channel is identified with in the registry
     * @param channel the channel to register
     * @param registerCommands if commands for the channel should be registered
     * @since 2.1.0
     */
    void register(final Key key, final ChatChannel channel, final boolean registerCommands);

}
