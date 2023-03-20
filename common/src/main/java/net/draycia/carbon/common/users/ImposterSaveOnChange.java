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
package net.draycia.carbon.common.users;

import java.util.UUID;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ImposterSaveOnChange implements SaveOnChange {

    private static final SaveOnChange IMPOSTER = new ImposterSaveOnChange();

    @Override
    public int saveDisplayName(UUID id, @Nullable Component displayName) {
        return -1;
    }

    @Override
    public int saveMuted(UUID id, boolean muted) {
        return -1;
    }

    @Override
    public int saveDeafened(UUID id, boolean deafened) {
        return -1;
    }

    @Override
    public int saveSpying(UUID id, boolean spying) {
        return -1;
    }

    @Override
    public int saveSelectedChannel(UUID id, @Nullable Key selectedChannel) {
        return -1;
    }

    @Override
    public int saveLastWhisperTarget(UUID id, @Nullable UUID lastWhisperTarget) {
        return -1;
    }

    @Override
    public int saveWhisperReplyTarget(UUID id, @Nullable UUID whisperReplyTarget) {
        return -1;
    }

    @Override
    public int addIgnore(UUID id, UUID ignoredPlayer) {
        return -1;
    }

    @Override
    public int removeIgnore(UUID id, UUID ignoredPlayer) {
        return -1;
    }

    @Override
    public int addLeftChannel(UUID id, Key channel) {
        return -1;
    }

    @Override
    public int removeLeftChannel(UUID id, Key channel) {
        return -1;
    }

    public static SaveOnChange impersonateIfNeeded(final UserManager<?> userManager) {
        if (userManager instanceof SaveOnChange saveOnChange) {
            return saveOnChange;
        } else {
            return IMPOSTER;
        }
    }

}
