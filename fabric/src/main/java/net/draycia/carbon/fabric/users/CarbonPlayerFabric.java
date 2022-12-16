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
package net.draycia.carbon.fabric.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.InventorySlot;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.WrappedCarbonPlayer;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
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
        return this.player().orElseThrow();
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
        if (this.player().isEmpty()) {
            return Locale.getDefault();
        }

        return this.player().get().get(Identity.LOCALE).orElse(Locale.getDefault());
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
    public double distanceSquaredFrom(final CarbonPlayer other) {
        if (this.player().isEmpty()) {
            return -1;
        }

        final @Nullable ServerPlayer player = this.player().orElse(null);
        final @Nullable ServerPlayer otherPlayer = this.carbonChatFabric.minecraftServer()
            .getPlayerList().getPlayer(other.uuid());

        if (player == null || otherPlayer == null) {
            return -1;
        }

        final double deltaX = player.position().x() - otherPlayer.position().x();
        final double deltaY = player.position().y() - otherPlayer.position().y();
        final double deltaZ = player.position().z() - otherPlayer.position().z();

        return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ);
    }

    @Override
    public boolean sameWorldAs(final CarbonPlayer other) {
        if (this.player().isEmpty()) {
            return false;
        }

        final Optional<ServerPlayer> player = this.player();
        final @Nullable ServerPlayer otherPlayer = this.carbonChatFabric.minecraftServer()
            .getPlayerList().getPlayer(other.uuid());

        if (player.isEmpty() || otherPlayer == null) {
            return false;
        }

        return player.get().getLevel().equals(otherPlayer.getLevel());
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

        return item.getDisplayName().asComponent();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return this.player()
            .map(player -> Permissions.check(player, permission, player.server.getOperatorUserPermissionLevel()))
            .orElse(false);
    }

    @Override
    public String primaryGroup() {
        if (!this.carbonChatFabric.luckPermsLoaded()) {
            return "default";
        }

        return this.user().getPrimaryGroup();
    }

    @Override
    public List<String> groups() {
        if (!this.carbonChatFabric.luckPermsLoaded()) {
            return List.of("default");
        }

        final var groups = new ArrayList<String>();

        for (final var group : this.user().getInheritedGroups(this.user().getQueryOptions())) {
            groups.add(group.getName());
        }

        return groups;
    }

}
