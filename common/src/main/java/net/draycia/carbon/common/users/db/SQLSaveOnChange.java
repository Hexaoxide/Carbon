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
package net.draycia.carbon.common.users.db;

import java.util.UUID;
import net.draycia.carbon.common.users.SaveOnChange;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@DefaultQualifier(NonNull.class)
public interface SQLSaveOnChange extends SaveOnChange {

    @SqlUpdate("UPDATE carbon_users SET (displayname = :displayname) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveDisplayName(final UUID id, final @Nullable Component component);

    @SqlUpdate("UPDATE carbon_users SET (muted = :muted) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveMuted(final UUID id, final boolean muted);

    @SqlUpdate("UPDATE carbon_users SET (deafened = :deafened) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveDeafened(final UUID id, final boolean deafened);

    @SqlUpdate("UPDATE carbon_users SET (spying = :spying) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveSpying(final UUID id, final boolean spying);

    @SqlUpdate("UPDATE carbon_users SET selectedchannel = :selectedChannel WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveSelectedChannel(final UUID id, final @Nullable Key selectedChannel);

    @SqlUpdate("UPDATE carbon_users SET (lastwhispertarget = :lastWhisperTarget) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveLastWhisperTarget(final UUID id, final @Nullable UUID lastWhisperTarget);

    @SqlUpdate("UPDATE carbon_users SET (whisperreplytarget = :whisperReplyTarget) WHERE id = UNHEX(REPLACE(:id, '-', ''))")
    int saveWhisperReplyTarget(final UUID id, final @Nullable UUID whisperReplyTarget);

    @SqlUpdate("INSERT INTO carbon_ignores VALUES (id = UNHEX(REPLACE(:id, '-', '')), ignoredplayer = :ignoredPlayer)")
    int addIgnore(final UUID id, final UUID ignoredPlayer);

    @SqlUpdate("DELETE FROM carbon_ignores WHERE (id = UNHEX(REPLACE(:id, '-', '')), ignoredplayer = UNHEX(REPLACE(:ignoredPlayer, '-', '')))")
    int removeIgnore(final UUID id, final UUID ignoredPlayer);

}
