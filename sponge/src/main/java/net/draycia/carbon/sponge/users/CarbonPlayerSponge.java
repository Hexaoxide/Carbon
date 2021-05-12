package net.draycia.carbon.sponge.users;

import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class CarbonPlayerSponge extends CarbonPlayerCommon {

  public CarbonPlayerSponge(
    final @NonNull String username,
    final @NonNull Component displayName,
    final @NonNull UUID uuid
  ) {
    super(username, displayName, uuid, Identity.identity(uuid));
  }

  @Override
  public @NonNull Audience audience() {
    return this.player()
      .map(player -> (Audience) player)
      .orElseGet(Audience::empty);
  }

  private @NonNull Optional<ServerPlayer> player() {
    return Sponge.server().player(this.uuid);
  }

}
