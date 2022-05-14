/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.api.util;

import java.util.List;

/**
 * A slot in a player's inventory.
 *
 * @since 2.0.0
 */
public final class InventorySlot {

    /**
     * An {@link InventorySlot} instance, usable in chat with the given placeholders.
     *
     * @param placeholders the placeholders that can be used in chat
     * @return the instance
     * @since 2.0.0
     */
    public static InventorySlot of(final String... placeholders) {
        return new InventorySlot(placeholders);
    }

    private List<String> placeholders;

    private InventorySlot() {

    }

    private InventorySlot(final String... placeholders) {
        this.placeholders = List.of(placeholders);
    }

    /**
     * Returns this slot's placeholders, which can be used in chat to show the item in said slot.
     *
     * @return this slot's placeholders
     * @since 2.0.0
     */
    public List<String> placeholders() {
        return this.placeholders;
    }

    public static final InventorySlot HELMET = InventorySlot.of("helm", "helmet", "hat", "head");
    public static final InventorySlot CHEST = InventorySlot.of("chest", "chestplate");
    public static final InventorySlot LEGS = InventorySlot.of("legs", "leggings");
    public static final InventorySlot BOOTS = InventorySlot.of("boots", "feet");
    public static final InventorySlot MAIN_HAND = InventorySlot.of("main_hand", "hand", "item");
    public static final InventorySlot OFF_HAND = InventorySlot.of("off_hand");

    public static List<InventorySlot> SLOTS = List.of(HELMET, CHEST, LEGS, BOOTS, MAIN_HAND, OFF_HAND);

}
