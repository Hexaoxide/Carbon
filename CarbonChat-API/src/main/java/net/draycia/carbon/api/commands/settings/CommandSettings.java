package net.draycia.carbon.api.commands.settings;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public class CommandSettings {

  @Setting
  private boolean enabled;

  @Setting
  private @MonotonicNonNull String name;

  @Setting
  private @MonotonicNonNull Set<@NonNull String> aliases;

  public @NonNull Set<@NonNull String> aliases() {
    return this.aliases;
  }

  public @NonNull String name() {
    return this.name;
  }

  public boolean enabled() {
    return this.enabled;
  }
}
