package net.draycia.carbon.bukkit.listeners.events;

import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.bukkit.util.CarbonUtils;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.ChatComponentEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.event.PostOrders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class ItemLinkHandler {

  public ItemLinkHandler() {
    CarbonEvents.register(ChatComponentEvent.class, PostOrders.FIRST, false, event -> {
      // Handle item linking placeholders
      final Player player = Bukkit.getPlayer(event.sender().uuid());

      if (player == null) {
        return;
      }

      if (!player.hasPermission("carbonchat.itemlink")) {
        return;
      }

      final Component itemComponent = CarbonUtils.createComponent(player);

      if (itemComponent.equals(Component.empty())) {
        return;
      }

      for (final Pattern pattern : event.channel().itemLinkPatterns()) {
        if (pattern.matcher(event.originalMessage()).find()) {
          final TextComponent component = (TextComponent) event.component()
            .replaceText(configurer -> configurer.once().match(pattern).replacement(itemComponent));
          event.component(component);

          break;
        }
      }
    });

    CarbonEvents.register(ChatFormatEvent.class, PostOrders.EARLY, false, event -> {
      for (final Pattern pattern : event.channel().itemLinkPatterns()) {
        event.message(pattern.matcher(event.message()).replaceAll("<white>$1</white>"));
      }
    });
  }

}
