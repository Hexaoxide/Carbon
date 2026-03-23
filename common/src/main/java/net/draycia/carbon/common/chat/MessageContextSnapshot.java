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
package net.draycia.carbon.common.chat;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Immutable snapshot of all player and channel state captured at message
 * decoration time ({@code AsyncChatDecorateEvent}, LOWEST priority).
 *
 * <p>By freezing the sender reference, channel selection, and quick-prefix
 * flag at decoration time, the rendering phase ({@code AsyncChatEvent},
 * HIGHEST priority) is guaranteed to use exactly the same state regardless
 * of any permission updates, cache invalidations, or network changes that
 * may occur between the two events.</p>
 *
 * <p>This prevents the "Checksum mismatch on last seen update" Paper
 * disconnect that arises when player state (e.g. LuckPerms permission
 * update, remote server shutdown) changes between decoration and render.</p>
 *
 * @param sender        the resolved sender, captured once at decoration time
 * @param channelKey    the chat channel key selected for this message
 * @param usedQuickPrefix whether a quick-prefix channel switch was used
 * @param placeholderResolutionSnapshot sender/recipient placeholder substitutions captured for this message
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public record MessageContextSnapshot(
    CarbonPlayer sender,
    Key channelKey,
    boolean usedQuickPrefix,
    PlaceholderResolutionSnapshot placeholderResolutionSnapshot
) {

}
