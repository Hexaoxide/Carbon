package net.draycia.carbon.bukkit.users;

import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class CarbonPlayerBukkit extends CarbonPlayerCommon {

  public CarbonPlayerBukkit(
    final @NonNull String username,
    final @NonNull Component displayName,
    final @NonNull UUID uuid
  ) {
    super(username, displayName, uuid, Identity.identity(uuid));
  }

  @Override
  public @NonNull Audience audience() {
    final Player player = this.player();

    if (player == null) {
      return Audience.empty();
    }

    return player;
  }

  private @Nullable Player player() {
    return Bukkit.getPlayer(this.uuid);
  }

}
