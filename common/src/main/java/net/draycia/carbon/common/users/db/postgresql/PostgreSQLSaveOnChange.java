package net.draycia.carbon.common.users.db.postgresql;

import java.util.UUID;
import net.draycia.carbon.common.users.SaveOnChange;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@DefaultQualifier(NonNull.class)
public interface PostgreSQLSaveOnChange extends SaveOnChange {

    @SqlUpdate("")
    int saveDisplayName(final UUID id, final @Nullable Component displayName);

    @SqlUpdate("")
    int saveMuted(final UUID id, final boolean muted);

    @SqlUpdate("")
    int saveDeafened(final UUID id, final boolean deafened);

    @SqlUpdate("")
    int saveSpying(final UUID id, final boolean spying);

    @SqlUpdate("")
    int saveSelectedChannel(final UUID id, final @Nullable Key selectedChannel);

    @SqlUpdate("")
    int saveLastWhisperTarget(final UUID id, final @Nullable UUID lastWhisperTarget);

    @SqlUpdate("")
    int saveWhisperReplyTarget(final UUID id, final @Nullable UUID whisperReplyTarget);

    @SqlUpdate("")
    int addIgnore(final UUID id, final UUID ignoredPlayer);

    @SqlUpdate("")
    int removeIgnore(final UUID id, final UUID ignoredPlayer);

}
