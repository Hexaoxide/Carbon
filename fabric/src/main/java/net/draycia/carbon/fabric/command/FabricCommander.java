package net.draycia.carbon.fabric.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface FabricCommander extends Commander, ForwardingAudience.Single {

    static FabricCommander from(final CommandSourceStack commandSourceStack) {
        return new FabricCommanderImpl(commandSourceStack);
    }

    CommandSourceStack commandSourceStack();

    @Override
    default Audience audience() {
        return FabricServerAudiences.of(this.commandSourceStack().getServer()).audience(this.commandSourceStack());
    }

    record FabricCommanderImpl(CommandSourceStack commandSourceStack) implements FabricCommander {
    }

}
