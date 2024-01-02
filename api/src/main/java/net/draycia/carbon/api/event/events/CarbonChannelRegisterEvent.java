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

import java.util.Set;
import java.util.function.Consumer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.event.CarbonEvent;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * {@link CarbonEvent} that's called after new channels are registered.
 *
 * <p>Note that some invocations of this event may be too early for
 * API consumers to be notified. {@link ChannelRegistry#allKeys(Consumer)}
 * is provided as a helper for when knowledge of all registered channels
 * is needed.</p>
 *
 * @since 3.0.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonChannelRegisterEvent extends CarbonEvent {

    /**
     * Gets the channel registry.
     *
     * @return the channel registry
     * @since 3.0.0
     */
    ChannelRegistry channelRegistry();

    /**
     * Gets the key(s) that were registered to trigger this event.
     *
     * @return key(s) registered
     * @since 3.0.0
     */
    Set<Key> registered();

}
