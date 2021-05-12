package net.draycia.carbon.bukkit;

import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.users.MemoryUserManagerBukkit;
import net.draycia.carbon.common.CarbonChatCommon;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;

public final class CarbonChatBukkit extends CarbonChatCommon {

  private final @NonNull UserManager userManager = new MemoryUserManagerBukkit();

  @Override
  public @NonNull UserManager userManager() {
    return this.userManager;
  }

  @Override
  public @NonNull Component createItemHoverComponent(final @NonNull UUID uuid) {
    final Player player = Bukkit.getPlayer(uuid); // This is temporary (it's not)

    if (player == null) {
      return empty();
    }

    final ItemStack itemStack;

    final ItemStack mainHand = player.getInventory().getItemInMainHand();

    if (mainHand != null && !mainHand.getType().isAir()) {
      itemStack = mainHand;
    } else {
      final ItemStack offHand = player.getInventory().getItemInMainHand();

      if (offHand != null && !offHand.getType().isAir()) {
        itemStack = offHand;
      } else {
        itemStack = null;
      }
    }

    if (itemStack == null) {
      return empty();
    }

    if (itemStack.getType().isAir()) {
      return empty();
    }

    final Component displayName = requireNonNullElseGet(itemStack.getItemMeta().displayName(), () ->
      translatable(itemStack.getType().getTranslationKey()));

    return createItemHoverComponent(displayName, itemStack);
  }

}
