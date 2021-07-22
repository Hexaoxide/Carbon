package net.draycia.carbon.sponge.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;

@DefaultQualifier(NonNull.class)
public interface SpongeCommander extends Commander, ForwardingAudience.Single {

    static SpongeCommander from(final CommandCause commandCause) {
        return new SpongeCommanderImpl(commandCause);
    }

    @NonNull CommandCause commandCause();

    record SpongeCommanderImpl(CommandCause commandCause) implements SpongeCommander {

        @Override
        public @NotNull Audience audience() {
            return this.commandCause.audience();
        }

    }

}
