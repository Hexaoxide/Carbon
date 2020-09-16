package net.draycia.carbon.api.commands;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class CommandSettings {

  @Setting
  private final boolean enabled;

  @Setting
  private @NonNull final String name;

  @Setting
  private @NonNull final List<@NonNull String> aliases;

  public CommandSettings(final boolean enabled, @NonNull final String name, @NonNull final List<@NonNull String> aliases) {
    this.enabled = enabled;
    this.name = name;
    this.aliases = aliases;
  }

  public @NonNull String @NonNull [] aliases() {
    return this.aliases.toArray(new String[0]);
  }

  @NonNull
  public String name() {
    return this.name;
  }

  public boolean enabled() {
    return this.enabled;
  }
}
