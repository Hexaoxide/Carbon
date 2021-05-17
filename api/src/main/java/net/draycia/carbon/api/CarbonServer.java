package net.draycia.carbon.api;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;

@DefaultQualifier(NonNull.class)
public interface CarbonServer extends Audience {

    Audience console();

    Iterable<? extends CarbonPlayer> players();

    @Nullable CarbonPlayer player(final UUID uuid);

    @Nullable CarbonPlayer player(final String username);

}
