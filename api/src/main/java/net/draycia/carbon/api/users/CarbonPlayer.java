package net.draycia.carbon.api.users;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class CarbonPlayer implements Audience, Identified {
  public abstract @NonNull Component displayName();
}
