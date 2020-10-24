package net.draycia.carbon.api.commands.settings;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Collections;
import java.util.Set;

@ConfigSerializable
public class CommandSettings {

  @Setting
  private boolean enabled = true;

  @Setting
  private @NonNull String name = "command";

  @Setting
  private @NonNull Set<@NonNull String> aliases = Collections.emptySet();

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
