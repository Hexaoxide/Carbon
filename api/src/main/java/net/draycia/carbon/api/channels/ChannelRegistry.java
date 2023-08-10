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
package net.draycia.carbon.api.channels;

import java.util.Set;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Registry for chat channels.
 *
 * @since 2.0.0
 */
public interface ChannelRegistry {

    /**
     * Registers the chat channel with its key.
     *
     * <p>Registrations will persist when reloading Carbon's configuration.</p>
     *
     * @param channel the channel to register
     * @since 2.1.0
     */
    void register(ChatChannel channel);

    /**
     * Gets the channel with the matching key.
     *
     * @param key the channel's key
     * @return the channel
     * @since 2.1.0
     */
    @Nullable ChatChannel channel(Key key);

    /**
     * Gets the default key.
     *
     * @return the default key
     * @since 2.1.0
     */
    @NonNull Key defaultKey();

    /**
     * Gets the default value.
     *
     * @return the default value
     * @since 2.1.0
     */
    @NonNull ChatChannel defaultChannel();

    /**
     * Gets the list of registered channel keys.
     *
     * @return the registered channel keys
     * @since 2.1.0
     */
    @NonNull Set<Key> keys();

    /**
     * Gets the channel with the matching key, otherwise the default channel.
     *
     * @param key the channel key
     * @return the channel, or the default one
     * @since 2.1.0
     */
    ChatChannel keyOrDefault(Key key);

    /**
     * The provided action will be executed immediately for all currently registered
     * channels.
     *
     * <p>When new channels are registered, the action will be invoked again for each new channel.</p>
     *
     * @param action action
     * @since 2.1.0
     */
    void allKeys(Consumer<Key> action);

}
