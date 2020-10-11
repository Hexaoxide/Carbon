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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class CarbonUtils {

  private static final @NonNull String @NonNull [] colors;

  private CarbonUtils() {

  }

  static {
    final List<String> colorList = new ArrayList<>();

    for (final NamedTextColor color : NamedTextColor.values()) {
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

    final Content content = Bukkit.getItemFactory().hoverContentOf(itemStack);
    final HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, content);

    final ComponentBuilder component = new ComponentBuilder();
    component.event(event); // Let this be inherited by all coming components.
    component.color(ChatColor.WHITE).append("[");

    if (itemStack.getItemMeta().hasDisplayName()) {
      component.append(TextComponent.fromLegacyText(itemStack.getItemMeta().getDisplayName()));
    } else {
      // As of 1.13, Material is 1:1 with MC's names
      final String prefix = itemStack.getType().isBlock() ? "block" : "item";
      final String name = prefix + ".minecraft." + itemStack.getType().name().toLowerCase();

      component.append(new TranslatableComponent(name));
    }

    component.color(ChatColor.WHITE).append("]");

    return BungeeCordComponentSerializer.get().deserialize(component.create());
  }

  @Nullable
  public static TextColor parseColor(@Nullable final String input) {
    return parseColor(null, input);
  }

  @Nullable
  public static TextColor parseColor(@Nullable final ChatUser user, @Nullable String input) {
    if (input == null) {
      input = "white";
    }

    if (user != null && user.online()) {
      input = PlaceholderAPI.setPlaceholders(user.player(), input);
    }

    for (final NamedTextColor namedColor : NamedTextColor.values()) {
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

  private static final @NonNull Pattern spigotLegacyRGB =
    Pattern.compile("[§&]x[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])");

  private static final @NonNull Pattern pluginRGB =
    Pattern.compile("[§&]#([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])([0-9a-f])");

  @NonNull
  public static String translateAlternateColors(@NonNull String input) {
    // TODO: check if MiniMessage or MineDown
    String output = input;

    // Legacy RGB
    output = spigotLegacyRGB.matcher(output).replaceAll("<#$1$2$3$4$5$6>");

    // Alternate RGB, TAB (neznamy) && KiteBoard
    output = pluginRGB.matcher(output).replaceAll("<#$1$2$3$4$5$6>");

    // Legacy Colors
    for (final char c : "0123456789abcdef".toCharArray()) {
      final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
      final TextColor color = format.color();

      output = output.replaceAll("[§&]" + c, "<" + color.asHexString() + ">");
    }

    // Legacy Formatting
    for (final char c : "klmno".toCharArray()) {
      final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
      final TextDecoration decoration = format.decoration();

      output = output.replaceAll("[§&]" + c, "<" + decoration.name() + ">");
    }

    return output;
  }

  @NonNull
  public static Argument onlineChatUserArgument() {
    return new CustomArgument<>(input -> {
      final CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

      final Player player = Bukkit.getPlayer(input);

      if (player == null) {
        throw new CustomArgument.CustomArgumentException("Invalid player for input (" + input + ")");
      }

      return carbonChat.userService().wrap(player);
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
    return new CustomArgument<>(input -> {
      final CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

      return carbonChat.userService().wrap(input);
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
    return new CustomArgument<>(input -> {
      final TextColor color = parseColor(input);

      if (color == null) {
        throw new CustomArgument.CustomArgumentException("Invalid color for input (" + input + ")");
      }

      return color;
    }).overrideSuggestions(colors);
  }

  @NonNull
  public static Argument channelArgument() {
    return new CustomArgument<>(input -> {
      final CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");

      final ChatChannel channel = carbonChat.channelManager().registry().channel(input);

      if (channel == null) {
        throw new CustomArgument.CustomArgumentException("Invalid channel for input (" + input + ")");
      }

      return channel;
      //        }).overrideSuggestions((sender, args) -> {
      //            CarbonChat carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
      //
      //            ArrayList<String> channels = new ArrayList<>();
      //
      //            for (ChatChannel channel : this.carbonChat.getChannelManager().getRegistry().values()) {
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
