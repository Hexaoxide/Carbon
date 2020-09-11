package net.draycia.carbon.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import org.bukkit.entity.Player;

public class RelationalPlaceholderHandler {

  public RelationalPlaceholderHandler() {
    CarbonEvents.register(ChatFormatEvent.class, new EventSubscriber<ChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final ChatFormatEvent event) {
        if (event.target() == null) {
          return;
        }

        if (!event.sender().online() || !event.target().online()) {
          return;
        }

        final Player sender = event.sender().player();
        final Player target = event.target().player();

        event.format(PlaceholderAPI.setRelationalPlaceholders(sender, target, event.format()));
      }
    });
  }

}
