/*
    Simple channel based chat plugin for Spigot
    Copyright (C) 2020 Alexander Söderberg
    Copyright (C) 2020 Josua Parks (Draycia)

    Modifications made to work with Kyori/Adventure

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.bukkit.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CarbonUtils {

  private CarbonUtils() {

  }

  public static @NonNull Component createComponent(final @NonNull Player player) {
    if (!FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
      return net.kyori.adventure.text.Component.empty();
    }

    final ItemStack itemStack = player.getInventory().getItemInMainHand();

    if (itemStack.getType().isAir()) {
      return net.kyori.adventure.text.Component.empty();
    }

    final HoverEvent<HoverEvent.ShowItem> hoverEvent = itemStack.asHoverEvent();
    Component component = Component.text("[").color(NamedTextColor.WHITE).hoverEvent(hoverEvent);

    final Component displayName = itemStack.getItemMeta().displayName();

    if (displayName != null) {
      component = component.append(displayName);
    } else {
      // As of 1.13, Material is 1:1 with MC's names
      final String prefix = itemStack.getType().isBlock() ? "block" : "item";
      final String name = prefix + ".minecraft." + itemStack.getType().name().toLowerCase();

      component = component.append(Component.translatable(name));
    }

    return component.append(Component.text("]").color(NamedTextColor.WHITE));
  }

}
