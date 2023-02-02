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
package net.draycia.carbon.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Static accessor for the {@link CarbonChat} class.
 *
 * @since 1.0.0
 */
@DefaultQualifier(NonNull.class)
public final class CarbonChatProvider {

    private static @Nullable CarbonChat instance;

    private CarbonChatProvider() {

    }

    /**
     * Registers the {@link CarbonChat} implementation.
     *
     * @param carbonChat the carbon implementation
     * @since 1.0.0
     */
    public static void register(final CarbonChat carbonChat) {
        CarbonChatProvider.instance = carbonChat;
    }

    /**
     * Gets the currently registered {@link CarbonChat} implementation.
     *
     * @return the registered carbon implementation
     * @since 1.0.0
     */
    public static CarbonChat carbonChat() {
        if (CarbonChatProvider.instance == null) {
            throw new IllegalStateException("CarbonChat not initialized!"); // LuckPerms design go brrrr
        }

        return CarbonChatProvider.instance;
    }

}
