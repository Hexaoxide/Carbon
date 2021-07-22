package net.draycia.carbon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public interface VelocityCommander extends Commander, ForwardingAudience.Single {

    static VelocityCommander from(final CommandSource source) {
        return new VelocityCommanderImpl(source);
    }

    CommandSource commandSource();

    record VelocityCommanderImpl(CommandSource commandSource) implements VelocityCommander {

        @Override
        public @NotNull Audience audience() {
            return this.commandSource;
        }

    }

}
