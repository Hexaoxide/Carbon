package net.draycia.carbon.bukkit.users;

import net.draycia.carbon.api.users.ConsoleUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitConsoleUser implements ConsoleUser, ForwardingAudience.Single {

  private @NonNull final Audience audience;

  public BukkitConsoleUser(@NonNull final ConsoleCommandSender sender) {
    final Plugin plugin = Bukkit.getPluginManager().getPlugin("CarbonChat");

    this.audience = BukkitAudiences.create(plugin).sender(sender);
  }

  @Override
  public @NonNull Identity identity() {
    return Identity.nil();
  }

  @Override
  public @NonNull Audience audience() {
    return this.audience;
  }

  @Override
  public boolean hasPermission(@NonNull final String permission) {
    return true;
  }

  @Override
  public @NonNull String name() {
    return "Console";
  }

}
