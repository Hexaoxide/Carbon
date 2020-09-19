package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.event.PostOrders;

public class CustomPlaceholderHandler {

  public CustomPlaceholderHandler() {
    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();

    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      final ConfigurationSection placeholders = carbonChat.getConfig().getConfigurationSection("placeholders");

      for (final String key : placeholders.getKeys(false)) {
        final String value = placeholders.getString(key);

        event.format(event.format().replace("<" + key + ">", value));
      }
    });
  }

}
