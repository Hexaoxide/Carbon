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
package net.draycia.carbon.api.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * A {@link ChatComponentRenderer chat renderer} that's identifiable by key.
 *
 * @since 2.0.0
 */
@DefaultQualifier(NonNull.class)
public interface KeyedRenderer extends Keyed, ChatComponentRenderer {

    /**
     * Creates a new renderer with the corresponding key.
     *
     * @param key      the renderer's key
     * @param renderer the chat renderer
     * @return the keyed renderer
     * @since 2.0.0
     */
    static KeyedRenderer keyedRenderer(final Key key, final ChatComponentRenderer renderer) {
        return new KeyedRendererImpl(key, renderer);
    }

}
