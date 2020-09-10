package net.draycia.carbon.listeners;

import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

public class ItemLinkHandler implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onItemLink(final ChatComponentEvent event) {
    // Handle item linking placeholders
    if (event.sender().online()) {
      final Player player = event.sender().player();

      if (!player.hasPermission("carbonchat.itemlink")) {
        return;
      }

      final Component itemComponent = CarbonUtils.createComponent(player);

      if (itemComponent.equals(TextComponent.empty())) {
        return;
      }

      for (final Pattern pattern : event.channel().itemLinkPatterns()) {
        final String patternContent = pattern.toString().replace("\\Q", "").replace("\\E", "");

        if (event.originalMessage().contains(patternContent)) {
          final TextComponent component = (TextComponent) event.component().replaceFirstText(pattern, input -> {
            return TextComponent.builder().append(itemComponent);
          });

          event.component(component);
          break;
        }
      }
    }
  }

}
