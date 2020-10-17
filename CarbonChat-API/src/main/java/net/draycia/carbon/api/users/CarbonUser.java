package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CarbonUser extends Audience, Identified {

  boolean hasPermission(@NonNull String permission);

  @NonNull String name();

}
