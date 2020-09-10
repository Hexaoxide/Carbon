package net.draycia.carbon.listeners;

import java.util.regex.Pattern;
import net.draycia.carbon.events.ChatComponentEvent;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ItemLinkHandler implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onItemLink(ChatComponentEvent event) {
    // Handle item linking placeholders
    if (event.getSender().isOnline()) {
      Player player = event.getSender().asPlayer();

      if (!player.hasPermission("carbonchat.itemlink")) {
        return;
      }

      Component itemComponent = CarbonUtils.createComponent(player);

      if (itemComponent.equals(TextComponent.empty())) {
        return;
      }

      for (Pattern pattern : event.getChannel().getItemLinkPatterns()) {
        String patternContent = pattern.toString().replace("\\Q", "").replace("\\E", "");

        if (event.getOriginalMessage().contains(patternContent)) {
          TextComponent component =
              (TextComponent)
                  event
                      .getComponent()
                      .replaceFirstText(
                          pattern,
                          (input) -> {
                            return TextComponent.builder().append(itemComponent);
                          });

          event.setComponent(component);
          break;
        }
      }
    }
  }
}
