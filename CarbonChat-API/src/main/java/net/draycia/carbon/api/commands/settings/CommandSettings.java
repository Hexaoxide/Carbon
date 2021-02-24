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

  /**
   * The command's aliases
   */
  public @NonNull Set<@NonNull String> aliases() {
    return this.aliases;
  }

  /**
   * The command's name, also the command root
   */
  public @NonNull String name() {
    return this.name;
  }

  /**
   * If the command is enabled
   * Setting to false prevents registration of the command
   */
  public boolean enabled() {
    return this.enabled;
  }
}
