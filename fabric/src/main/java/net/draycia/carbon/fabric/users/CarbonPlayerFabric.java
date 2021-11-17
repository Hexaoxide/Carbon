package net.draycia.carbon.fabric.users;

import java.util.Locale;
import java.util.Optional;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.platform.fabric.PlayerLocales;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerFabric extends WrappedCarbonPlayer implements ForwardingAudience.Single {

    private final CarbonPlayerCommon carbonPlayerCommon;
    private final CarbonChatFabric carbonChatFabric;

    public CarbonPlayerFabric(final CarbonPlayerCommon carbonPlayerCommon, final CarbonChatFabric carbonChatFabric) {
        this.carbonPlayerCommon = carbonPlayerCommon;
        this.carbonChatFabric = carbonChatFabric;
    }

    @Override
    public @NonNull Audience audience() {
        return this.player()
            .map(p -> FabricServerAudiences.of(p.server).audience(p))
            .orElseThrow();
    }

    private Optional<ServerPlayer> player() {
        return Optional.ofNullable(
            this.carbonChatFabric.minecraftServer().getPlayerList()
                .getPlayer(this.carbonPlayerCommon.uuid())
        );
    }

    @Override
    public boolean vanished() {
        return false;
    }

    @Override
    public @Nullable Locale locale() {
        return this.player().map(PlayerLocales::locale).orElse(null);
    }

    @Override
    public boolean online() {
        return this.player().isPresent();
    }

    @Override
    public CarbonPlayerCommon carbonPlayerCommon() {
        return this.carbonPlayerCommon;
    }

    @Override
    public @Nullable Component createItemHoverComponent(final InventorySlot slot) {
        final Optional<ServerPlayer> playerOptional = this.player();
        if (playerOptional.isEmpty()) {
            return null;
        }
        final ServerPlayer player = playerOptional.get();

        final EquipmentSlot equipmentSlot;

        if (slot.equals(InventorySlot.MAIN_HAND)) {
            equipmentSlot = EquipmentSlot.MAINHAND;
        } else if (slot.equals(InventorySlot.OFF_HAND)) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        } else if (slot.equals(InventorySlot.HELMET)) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (slot.equals(InventorySlot.CHEST)) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (slot.equals(InventorySlot.LEGS)) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (slot.equals(InventorySlot.BOOTS)) {
            equipmentSlot = EquipmentSlot.FEET;
        } else {
            return null;
        }

        final @Nullable ItemStack item = player.getItemBySlot(equipmentSlot);

        if (item == null || item.isEmpty()) {
            return null;
        }

        return FabricServerAudiences.of(player.server).toAdventure(item.getDisplayName());
    }

}
