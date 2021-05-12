package net.draycia.carbon.sponge.command;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.PlayerCommander;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static java.util.Objects.requireNonNull;

public final class SpongePlayerCommander implements PlayerCommander, SpongeCommander {

  private final CarbonChat carbon;
  private final ServerPlayer player;
  private final CommandCause commandCause;

  public SpongePlayerCommander(final @NonNull CarbonChat carbon, final @NonNull ServerPlayer player, final @NonNull CommandCause commandCause) {
    this.carbon = carbon;
    this.player = player;
    this.commandCause = commandCause;
  }

  @Override
  public @NonNull CarbonPlayer carbonPlayer() {
    return requireNonNull(this.carbon.userManager().carbonPlayer(this.player.uniqueId()), "No CarbonPlayer for logged in Player!");
  }

  public @NonNull ServerPlayer player() {
    return this.player;
  }

  @Override
  public @NonNull CommandCause commandCause() {
    return this.commandCause;
  }

  @Override
  public @NonNull Audience audience() {
    return this.commandCause.audience();
  }

}
