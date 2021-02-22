package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CarbonUser extends Audience, Identified {

  boolean hasPermission(@NonNull String permission);

  @NonNull Component name();

  @NonNull String username();

}
