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
