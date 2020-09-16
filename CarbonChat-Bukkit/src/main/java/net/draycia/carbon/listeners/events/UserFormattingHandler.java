package net.draycia.carbon.listeners.events;

import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.event.PostOrders;

public class UserFormattingHandler {

  public UserFormattingHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!event.user().permissible()) {
        this.suppressFormatting(event);
        return;
      }

      if (!event.user().hasPermission("carbonchat.formatting") &&
        !event.user().hasPermission("carbonchat.channels." + event.channel().key() + ".formatting")) {
        this.suppressFormatting(event);
      } else {
        // Swap the &-style codes for minimessage-compatible strings
        event.message(MiniMessage.get().serialize(CarbonChatBukkit.LEGACY.deserialize(event.message())));
      }
    });
  }

  private void suppressFormatting(final PreChatFormatEvent event) {
    event.format(event.format().replace("<message>", "<pre><message></pre>"));
  }

}
