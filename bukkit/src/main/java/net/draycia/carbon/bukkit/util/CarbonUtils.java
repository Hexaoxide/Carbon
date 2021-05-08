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
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class CarbonUtils {

  private CarbonUtils() {

  }

  public static @NonNull Component createComponent(final @NonNull Player player) {
    final ItemStack itemStack = player.getInventory().getItemInMainHand();

    if (itemStack.getType().isAir()) {
      return empty();
    }

    final TextComponent.Builder builder = text();

    builder.hoverEvent(itemStack); // Let this be inherited by all coming components.
    builder.append(text("[", WHITE));

    final Component displayName = itemStack.getItemMeta().displayName();

    builder.append(requireNonNullElseGet(displayName, () ->
      translatable(itemStack.getType().getTranslationKey())));

    builder.append(text("]", WHITE));

    return builder.build();
  }

}
