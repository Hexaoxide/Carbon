package net.draycia.carbon.api.util;

import java.util.List;

public final class InventorySlot {

    public static InventorySlot of(final String... placeholders) {
        return new InventorySlot(placeholders);
    }

    private List<String> placeholders;

    private InventorySlot() {

    }

    private InventorySlot(final String... placeholders) {
        this.placeholders = List.of(placeholders);
    }

    public List<String> placeholders() {
        return this.placeholders;
    }

}
