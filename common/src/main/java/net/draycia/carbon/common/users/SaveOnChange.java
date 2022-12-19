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
package net.draycia.carbon.common.users;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface SaveOnChange {

    int saveDisplayName(final UUID id, final @Nullable Component displayName);

    int saveMuted(final UUID id, final boolean muted);

    int saveDeafened(final UUID id, final boolean deafened);

    int saveSpying(final UUID id, final boolean spying);

    int saveSelectedChannel(final UUID id, final @Nullable Key selectedChannel);

    int saveLastWhisperTarget(final UUID id, final @Nullable UUID lastWhisperTarget);

    int saveWhisperReplyTarget(final UUID id, final @Nullable UUID whisperReplyTarget);

    int addIgnore(final UUID id, final UUID ignoredPlayer);

    int removeIgnore(final UUID id, final UUID ignoredPlayer);

    int addLeftChannel(final UUID id, final Key channel);

    int removeLeftChannel(final UUID id, final Key channel);

}
