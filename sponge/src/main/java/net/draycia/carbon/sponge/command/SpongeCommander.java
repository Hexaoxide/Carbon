package net.draycia.carbon.sponge.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;

public interface SpongeCommander extends Commander, ForwardingAudience.Single {

  @NonNull CommandCause commandCause();

  static @NonNull SpongeCommander from(final @NonNull CommandCause commandCause) {
    return new SpongeCommanderImpl(commandCause);
  }

  record SpongeCommanderImpl(
    @NonNull CommandCause commandCause) implements SpongeCommander {

    @Override
    public @NonNull
    Audience audience() {
      return this.commandCause.audience();
    }

  }

}
