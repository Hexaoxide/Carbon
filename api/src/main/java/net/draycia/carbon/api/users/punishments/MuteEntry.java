package net.draycia.carbon.api.users.punishments;

import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;

@DefaultQualifier(NonNull.class)
public record MuteEntry(long muteEpoch, @Nullable UUID muteCause, long expirationEpoch, @Nullable Key channel) {

}
