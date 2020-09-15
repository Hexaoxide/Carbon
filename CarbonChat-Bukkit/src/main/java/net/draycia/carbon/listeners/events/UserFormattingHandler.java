package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.event.PostOrders;

public class UserFormattingHandler {

  public UserFormattingHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!event.user().online()) {
        this.suppressFormatting(event);
        return;
      }

      if (!event.user().player().hasPermission("carbonchat.formatting") &&
        !event.user().player().hasPermission("carbonchat.channels." + event.channel().key() + ".formatting")) {
        this.suppressFormatting(event);
      } else {
        // Swap the &-style codes for minimessage-compatible strings
        event.message(MiniMessage.get().serialize(CarbonChat.LEGACY.deserialize(event.message())));
      }
    });
  }

  private void suppressFormatting(final PreChatFormatEvent event) {
    event.format(event.format().replace("<message>", "<pre><message></pre>"));
  }

}
