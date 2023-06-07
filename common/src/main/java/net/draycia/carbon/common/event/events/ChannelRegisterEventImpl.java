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
package net.draycia.carbon.common.event.events;

import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEvent;
import net.draycia.carbon.api.event.events.CarbonChannelRegisterEvent;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * {@link CarbonEvent} that's called when channels are registered.
 *
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public class ChannelRegisterEventImpl implements CarbonChannelRegisterEvent {

    private final Iterable<ChatChannel> channels;
    private final CarbonChannelRegistry registry;

    /**
     * {@link CarbonEvent} that's called when channels are registered.
     *
     * @param channels the channels that were registered
     * @since 2.1.0
     */
    public ChannelRegisterEventImpl(final Iterable<ChatChannel> channels, final CarbonChannelRegistry registry) {
        this.channels = channels;
        this.registry = registry;
    }

    public Iterable<ChatChannel> channels() {
        return this.channels;
    }

    public void register(final Key key, final ChatChannel channel, final boolean registerCommands) {
        this.registry.register(key, channel);

        if (registerCommands) {
            this.registry.registerChannelCommands(channel);
        }
    }

}
