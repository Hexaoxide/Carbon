package net.draycia.carbon.bukkit.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface BukkitCommander extends Commander, ForwardingAudience.Single {

    static BukkitCommander from(final CommandSender sender) {
        return new BukkitCommanderImpl(sender);
    }

    CommandSender commandSender();

    record BukkitCommanderImpl(CommandSender commandSender) implements BukkitCommander {

        @Override
        public Audience audience() {
            return this.commandSender;
        }

    }

}
