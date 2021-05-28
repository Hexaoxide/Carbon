package net.draycia.carbon.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface VelocityCommander extends Commander, ForwardingAudience.Single {

    CommandSource commandSource();

    static VelocityCommander from(final CommandSource source) {
        return new VelocityCommanderImpl(source);
    }

    final class VelocityCommanderImpl implements VelocityCommander {

        private final CommandSource commandSource;

        private VelocityCommanderImpl(final CommandSource commandSource) {
            this.commandSource = commandSource;
        }

        @Override
        public CommandSource commandSource() {
            return this.commandSource;
        }

        @Override
        public Audience audience() {
            return this.commandSource;
        }

    }

}
