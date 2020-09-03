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
package net.draycia.carbon.util;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class CarbonUtils {

    public static Component createComponent(final Player player) {
        try {
            final ItemStack itemStack = player.getInventory().getItemInMainHand();

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return net.kyori.adventure.text.TextComponent.empty();
            }

            Content content = Bukkit.getItemFactory().hoverContentOf(itemStack);
            HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, content);

            ComponentBuilder component = new ComponentBuilder();
            component.color(ChatColor.WHITE).append("[");

            if (itemStack.getItemMeta().hasDisplayName()) {
                component.append(TextComponent.fromLegacyText(itemStack.getItemMeta().getDisplayName()));
            } else {
                component.append(new TranslatableComponent(itemStack.getItemMeta().getLocalizedName()));
            }

            component.color(ChatColor.WHITE).append("]");

            component.event(event);

            return BungeeCordComponentSerializer.get().deserialize(component.create());
        } catch (NoSuchMethodError ignored) {}

        return net.kyori.adventure.text.TextComponent.empty();
    }

    public static TextColor parseColor(ChatUser user, String input) {
        if (input == null) {
            input = "white";
        }

        if (user.isOnline()) {
            input = PlaceholderAPI.setPlaceholders(user.asPlayer(), input);
        }

        for (NamedTextColor namedColor : NamedTextColor.values()) {
            if (namedColor.toString().equalsIgnoreCase(input)) {
                return namedColor;
            }
        }

        if (input.contains("&") || input.contains("§")) {
            input = input.replace("&", "§");

            return LegacyComponentSerializer.legacySection().deserialize(input).color();
        }

        return TextColor.fromCSSHexString(input);
    }

    public static TextColor parseColor(String input) {
        if (input == null) {
            input = "white";
        }

        for (NamedTextColor namedColor : NamedTextColor.values()) {
            if (namedColor.toString().equalsIgnoreCase(input)) {
                return namedColor;
            }
        }

        if (input.contains("&") || input.contains("§")) {
            input = input.replace("&", "§");

            return LegacyComponentSerializer.legacySection().deserialize(input).color();
        }

        return TextColor.fromCSSHexString(input);
    }

    public static Argument onlineChatUserArgument() {
        return new CustomArgument<>((input) -> {
            CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

            Player player = Bukkit.getPlayer(input);

            if (player == null) {
                throw new CustomArgument.CustomArgumentException("Invalid player for input (" + input + ")");
            }

            return carbonChat.getUserService().wrap(player);
        }).overrideSuggestions((sender, args) -> {
            ArrayList<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }

            return players.toArray(new String[0]);
        });
    }

    public static Argument chatUserArgument() {
        return new CustomArgument<>((input) -> {
            CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

            return carbonChat.getUserService().wrap(input);
        }).overrideSuggestions((sender, args) -> {
            ArrayList<String> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }

            return players.toArray(new String[0]);
        });
    }

    private static String[] colors;

    static {
        ArrayList<String> colorList = new ArrayList<>();

        for (NamedTextColor color : NamedTextColor.values()) {
            colorList.add(color.toString());
        }

        colors = colorList.toArray(new String[0]);
    }

    public static Argument textColorArgument() {
        return new CustomArgument<>((input) -> {
            TextColor color =  parseColor(input);

            if (color == null) {
                throw new CustomArgument.CustomArgumentException("Invalid color for input (" + input + ")");
            }

            return color;
        }).overrideSuggestions(colors);
    }

    public static Argument channelArgument() {
        return new CustomArgument<>((input) -> {
            CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

            ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(input);

            if (channel == null) {
                throw new CustomArgument.CustomArgumentException("Invalid channel for input (" + input + ")");
            }

            return channel;
        }).overrideSuggestions((sender, args) -> {
            CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

            ArrayList<String> channels = new ArrayList<>();

            for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
                channels.add(channel.getKey());
            }

            return channels.toArray(new String[0]);
        });
    }

    public static Argument usableChannelArgument() {
        throw new NotImplementedException("Not implemented yet.");
    }

}