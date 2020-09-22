package net.draycia.carbon.api.commands.settings;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.List;
import java.util.Set;

@ConfigSerializable
public class CommandSettings {

  @Setting
  private final boolean enabled;

  @Setting
  private final @NonNull String name;

  @Setting
  private final @NonNull Set<@NonNull String> aliases;

  public CommandSettings(final boolean enabled, final @NonNull String name, final @NonNull Set<@NonNull String> aliases) {
    this.enabled = enabled;
    this.name = name;
    this.aliases = aliases;
  }

  public @NonNull Set<@NonNull String> aliases() {
    return this.aliases;
  }

  @NonNull
  public String name() {
    return this.name;
  }

  public boolean enabled() {
    return this.enabled;
  }
}
