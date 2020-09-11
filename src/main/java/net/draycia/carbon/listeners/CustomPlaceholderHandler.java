package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.PreChatFormatEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CustomPlaceholderHandler {

  public CustomPlaceholderHandler(@NonNull final CarbonChat carbonChat) {
    CarbonEvents.register(PreChatFormatEvent.class, new EventSubscriber<PreChatFormatEvent>() {
      @Override
      public int postOrder() {
        return PostOrders.FIRST;
      }

      @Override
      public boolean consumeCancelledEvents() {
        return false;
      }

      @Override
      public void invoke(final PreChatFormatEvent event) {
        final ConfigurationSection placeholders = carbonChat.getConfig().getConfigurationSection("placeholders");

        for (final String key : placeholders.getKeys(false)) {
          final String value = placeholders.getString(key);

          event.format(event.format().replace("<" + key + ">", value));
        }
      }
    });
  }

}
