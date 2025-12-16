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
package net.draycia.carbon.common.integration.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MiniPlaceholdersUtil {

    private static byte miniPlaceholdersLoaded = -1;

    private MiniPlaceholdersUtil() {
    }

    public static boolean miniPlaceholdersLoaded() {
        if (miniPlaceholdersLoaded == -1) {
            try {
                final String name = MiniPlaceholders.class.getName();
                Objects.requireNonNull(name);
                miniPlaceholdersLoaded = 1;
            } catch (final NoClassDefFoundError error) {
                miniPlaceholdersLoaded = 0;
            }
        }
        return miniPlaceholdersLoaded == 1;
    }

    public static Audience wrapAudiences(final @Nullable Audience recipient, final Audience sender) {
        if (!miniPlaceholdersLoaded()) {
            return sender;
        }
        return wrapAudiences_(recipient, sender);
    }

    private static Audience wrapAudiences_(final @Nullable Audience recipient, final Audience sender) {
        if (recipient == null) {
            return sender;
        }
        return RelationalAudience.from(recipient, sender);
    }

}
