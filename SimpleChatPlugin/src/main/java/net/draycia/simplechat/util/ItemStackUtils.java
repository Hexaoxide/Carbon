/*
    Simple channel based chat plugin for Spigot
    Copyright (C) 2020 Alexander Söderberg

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
package net.draycia.simplechat.util;

import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ItemStackUtils {

    private final Class<?> craftItemStackClass;
    private final Method asNMSCopyMethod;
    private final Class<?> itemStackClass;
    private final Method cMethod;

    public ItemStackUtils() throws NoSuchMethodException, ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName()
                .substring("org.bukkit.craftbukkit.".length());

        this.craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        this.asNMSCopyMethod = this.craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        this.itemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
        this.cMethod = this.itemStackClass.getMethod("C");
    }

    public Component createComponent(final Player player) {
        final ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return TextComponent.empty();
        }

        try {
            final Object cbItemStack = this.asNMSCopyMethod.invoke(null, itemStack);
            final Object mojangComponent = this.cMethod.invoke(cbItemStack);

            return MinecraftComponentSerializer.INSTANCE.deserialize(mojangComponent);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return TextComponent.empty();
        }
    }

}