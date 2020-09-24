package net.draycia.carbon.api.commands.settings;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public class CommandSettings {

  @Setting
  private boolean enabled;

  @Setting
  private @NonNull String name;

  @Setting
  private @NonNull Set<@NonNull String> aliases;

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
