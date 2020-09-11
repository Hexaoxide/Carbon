package net.draycia.carbon.listeners;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatComponentEvent;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class ItemLinkHandler {

  public ItemLinkHandler() {
    CarbonEvents.register(ChatComponentEvent.class, new EventSubscriber<ChatComponentEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final ChatComponentEvent event) {
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
    });
  }

}
