package net.draycia.carbon.sponge.users;

import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

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

  @Override
  public @NonNull Component createItemHoverComponent() {
    final ServerPlayer player = this.player().orElse(null);
    if (player == null) {
      return Component.empty();
    }

    final ItemStack itemStack;

    final ItemStack mainHand = player.itemInHand(HandTypes.MAIN_HAND);

    if (!mainHand.isEmpty()) {
      itemStack = mainHand;
    } else {
      final ItemStack offHand = player.itemInHand(HandTypes.OFF_HAND);

      if (!offHand.isEmpty()) {
        itemStack = offHand;
      } else {
        itemStack = null;
      }
    }

    if (itemStack == null || itemStack.isEmpty()) {
      return Component.empty();
    }

    final Component displayName = itemStack.get(Keys.DISPLAY_NAME)
      .orElse(itemStack.type().asComponent());

    return this.createItemHoverComponent(displayName, itemStack.createSnapshot());
  }

}
