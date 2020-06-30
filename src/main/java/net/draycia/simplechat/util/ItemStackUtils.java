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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagHolder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public final class ItemStackUtils {

    private final Class<?> craftItemStackClass;
    private final Method asNMSCopyMethod;
    private final Class<?> nbtTagCompoundClass;
    private final Constructor<?> nbtTagCompoundConstructor;
    private final Class<?> itemStackClass;
    private final Method saveMethod;

    public ItemStackUtils() throws NoSuchMethodException, ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName()
                .substring("org.bukkit.craftbukkit.".length());

        this.craftItemStackClass = cbClass(version, "inventory.CraftItemStack");
        this.asNMSCopyMethod = this.craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        this.nbtTagCompoundClass = nmsClass(version, "NBTTagCompound");
        this.nbtTagCompoundConstructor = this.nbtTagCompoundClass.getConstructor();
        this.itemStackClass = nmsClass(version, "ItemStack");
        this.saveMethod = this.itemStackClass.getMethod("save", this.nbtTagCompoundClass);
    }

    public TextComponent createComponent(final CommandSender player) {
        if (player instanceof Player) {
            final ItemStack itemStack = ((Player)player).getInventory().getItemInMainHand();

            if (itemStack.getType() == Material.AIR) {
                return TextComponent.empty();
            }

            final ItemMeta itemMeta;

            if (itemStack.hasItemMeta()) {
                itemMeta = itemStack.getItemMeta();
            } else {
                itemMeta = null;
            }

            final String name;

            if (itemMeta != null && itemMeta.hasDisplayName()) {
                name = itemMeta.getDisplayName();
            } else if (itemMeta != null && itemMeta.hasLocalizedName()) {
                name = itemMeta.getLocalizedName();
            } else {
                final Material material = itemStack.getType();
                final StringBuilder nameBuilder = new StringBuilder();
                final String[] nameParts = material.name().split(Pattern.quote("_"));
                for (int i = 0; i < nameParts.length; i++) {
                    nameBuilder.append(nameParts[i].charAt(0)).append(nameParts[i].substring(1).toLowerCase());
                    if ((i + 1) < nameParts.length) {
                        nameBuilder.append(" ");
                    }
                }
                name = nameBuilder.toString();
            }

            try {
                final Object cbItemStack = this.asNMSCopyMethod.invoke(null, itemStack);
                final Object nbtTag = this.nbtTagCompoundConstructor.newInstance();
                this.saveMethod.invoke(cbItemStack, nbtTag);
                final TextComponent nameComponent = LegacyComponentSerializer.legacy().deserialize(name);

                final Key key = Key.of(itemStack.getType().getKey().getKey());
                final int count = itemStack.getAmount();
                final BinaryTagHolder tagHolder = BinaryTagHolder.of(nbtTag.toString());

                return nameComponent.hoverEvent(HoverEvent.showItem(new HoverEvent.ShowItem(key, count, tagHolder)));
            } catch (final IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return TextComponent.empty();
            }
        } else {
            return TextComponent.empty();
        }
    }

    private static Class<?> cbClass(final String version, final String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }

    private static Class<?> nmsClass(final String version, final String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

}