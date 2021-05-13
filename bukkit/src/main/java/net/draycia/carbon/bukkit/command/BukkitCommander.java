package net.draycia.carbon.bukkit.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface BukkitCommander extends Commander, ForwardingAudience.Single {

  @NonNull CommandSender commandSender();

  static @NonNull BukkitCommander from(final @NonNull CommandSender sender) {
    return new BukkitCommanderImpl(sender);
  }

  final class BukkitCommanderImpl implements BukkitCommander {

    private final CommandSender commandSender;

    private BukkitCommanderImpl(final @NonNull CommandSender commandSender) {
      this.commandSender = commandSender;
    }

    public @NonNull CommandSender commandSender() {
      return this.commandSender;
    }

    @Override
    public @NonNull Audience audience() {
      return this.commandSender;
    }

  }

}
