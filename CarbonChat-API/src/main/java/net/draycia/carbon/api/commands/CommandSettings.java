package net.draycia.carbon.api.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandSettings {

  private final boolean enabled;

  @NonNull
  private final String name;

  @NonNull
  private final List<@NonNull String> aliases;

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
