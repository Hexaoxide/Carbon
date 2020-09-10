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
import java.util.ArrayList;
import java.util.List;
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
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CarbonUtils {

  private static final @NonNull String @NonNull [] colors;

  static {
    List<String> colorList = new ArrayList<>();

    for (NamedTextColor color : NamedTextColor.values()) {
      colorList.add(color.toString());
    }

    colors = colorList.toArray(new String[0]);
  }

  @NonNull
  public static Component createComponent(@NonNull final Player player) {
    if (!FunctionalityConstants.HAS_HOVER_EVENT_METHOD) {
      return net.kyori.adventure.text.TextComponent.empty();
    }

    final ItemStack itemStack = player.getInventory().getItemInMainHand();

    if (itemStack.getType().isAir()) {
      return net.kyori.adventure.text.TextComponent.empty();
    }

    Content content = Bukkit.getItemFactory().hoverContentOf(itemStack);
    HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, content);

    ComponentBuilder component = new ComponentBuilder();
    component.event(event); // Let this be inherited by all coming components.
    component.color(ChatColor.WHITE).append("[");

    if (itemStack.getItemMeta().hasDisplayName()) {
      component.append(TextComponent.fromLegacyText(itemStack.getItemMeta().getDisplayName()));
    } else {
      String name =
          itemStack.getItemMeta().hasDisplayName()
              ? itemStack.getItemMeta().getDisplayName()
              : "item.minecraft."
                  + itemStack
                      .getType()
                      .name()
                      .toLowerCase(); // As of 1.13, Material is 1:1 with MC's names
      component.append(new TranslatableComponent(name));
    }

    component.color(ChatColor.WHITE).append("]");

    return BungeeCordComponentSerializer.get().deserialize(component.create());
  }

  @Nullable
  public static TextColor parseColor(@Nullable String input) {
    return parseColor(null, input);
  }

  @Nullable
  public static TextColor parseColor(@Nullable ChatUser user, @Nullable String input) {
    if (input == null) {
      input = "white";
    }

    if (user != null && user.isOnline()) {
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

  @NonNull
  public static Argument onlineChatUserArgument() {
    return new CustomArgument<>(
        (input) -> {
          CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

          Player player = Bukkit.getPlayer(input);

          if (player == null) {
            throw new CustomArgument.CustomArgumentException(
                "Invalid player for input (" + input + ")");
          }

          return carbonChat.getUserService().wrap(player);
          //        }).overrideSuggestions((sender, args) -> {
          //            ArrayList<String> players = new ArrayList<>();
          //
          //            for (Player player : Bukkit.getOnlinePlayers()) {
          //                players.add(player.getName());
          //            }
          //
          //            return players.toArray(new String[0]);
        });
  }

  @NonNull
  public static Argument chatUserArgument() {
    return new CustomArgument<>(
        (input) -> {
          CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

          return carbonChat.getUserService().wrap(input);
          //        }).overrideSuggestions((sender, args) -> {
          //            ArrayList<String> players = new ArrayList<>();
          //
          //            for (Player player : Bukkit.getOnlinePlayers()) {
          //                players.add(player.getName());
          //            }
          //
          //            return players.toArray(new String[0]);
        });
  }

  @NonNull
  public static Argument textColorArgument() {
    return new CustomArgument<>(
            (input) -> {
              TextColor color = parseColor(input);

              if (color == null) {
                throw new CustomArgument.CustomArgumentException(
                    "Invalid color for input (" + input + ")");
              }

              return color;
            })
        .overrideSuggestions(colors);
  }

  @NonNull
  public static Argument channelArgument() {
    return new CustomArgument<>(
        (input) -> {
          CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

          ChatChannel channel = carbonChat.getChannelManager().getRegistry().get(input);

          if (channel == null) {
            throw new CustomArgument.CustomArgumentException(
                "Invalid channel for input (" + input + ")");
          }

          return channel;
          //        }).overrideSuggestions((sender, args) -> {
          //            CarbonChat carbonChat = (CarbonChat)
          // Bukkit.getPluginManager().getPlugin("CarbonChat");
          //
          //            ArrayList<String> channels = new ArrayList<>();
          //
          //            for (ChatChannel channel :
          // carbonChat.getChannelManager().getRegistry().values()) {
          //                channels.add(channel.getKey());
          //            }
          //
          //            return channels.toArray(new String[0]);
        });
  }

  @NonNull
  public static Argument usableChannelArgument() {
    throw new NotImplementedException("Not implemented yet.");
  }
}
