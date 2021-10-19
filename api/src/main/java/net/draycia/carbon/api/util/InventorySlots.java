package net.draycia.carbon.api.util;

import java.util.List;

public final class InventorySlots {

    public static final InventorySlot HELMET = InventorySlot.of("helm", "helmet", "hat", "head");
    public static final InventorySlot CHEST = InventorySlot.of("chest", "chestplate");
    public static final InventorySlot LEGS = InventorySlot.of("legs", "leggings");
    public static final InventorySlot BOOTS = InventorySlot.of("boots", "feet");
    public static final InventorySlot MAIN_HAND = InventorySlot.of("main_hand", "hand", "item");
    public static final InventorySlot OFF_HAND = InventorySlot.of("off_hand");

    public static List<InventorySlot> VALUES = List.of(HELMET, CHEST, LEGS, BOOTS, MAIN_HAND, OFF_HAND);

}
