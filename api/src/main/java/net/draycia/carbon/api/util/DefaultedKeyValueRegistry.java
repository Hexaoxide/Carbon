/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
package net.draycia.carbon.api.util;

import net.kyori.registry.DefaultedRegistryGetter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An extension of the DefaultedRegistryGetter that declares a defaultValue method.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @since 1.0.0
 */
public interface DefaultedKeyValueRegistry<K, V> extends DefaultedRegistryGetter<K, V> {

    /**
     * Gets the default value.
     *
     * @return the default value
     * @since 1.0.0
     */
    @NonNull V defaultValue();

}
