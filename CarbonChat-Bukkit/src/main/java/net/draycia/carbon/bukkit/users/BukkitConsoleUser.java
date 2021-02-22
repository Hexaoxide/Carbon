package net.draycia.carbon.bukkit.users;

import net.draycia.carbon.api.users.ConsoleUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitConsoleUser implements ConsoleUser, ForwardingAudience.Single {

  private final @NonNull Audience audience = Bukkit.getConsoleSender();

  @Override
  public @NonNull Identity identity() {
    return Identity.nil();
  }

  @Override
  public @NonNull Audience audience() {
    return this.audience;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return true;
  }

  @Override
  public @NonNull Component name() {
    return Component.text("Console");
  }

  @Override
  public @NonNull String username() {
    return "Console";
  }

}
