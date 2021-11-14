package net.draycia.carbon.fabric.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface FabricCommander extends Commander, ForwardingAudience.Single {

    static FabricCommander from(final MinecraftServer server, final CommandSourceStack commandSourceStack) {
        return new FabricCommanderImpl(server, commandSourceStack);
    }

    CommandSourceStack commandSourceStack();

    record FabricCommanderImpl(MinecraftServer server, CommandSourceStack commandSourceStack) implements FabricCommander {

        // TODO: Client support, middleman the FabricServerAudiences and return client or server depending on env?
        @Override
        public Audience audience() {
            return FabricServerAudiences.of(this.server).audience(this.commandSourceStack);
        }

    }

}
