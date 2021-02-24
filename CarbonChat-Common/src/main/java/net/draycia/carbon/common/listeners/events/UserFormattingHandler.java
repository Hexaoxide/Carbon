package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.event.PostOrders;

public class UserFormattingHandler {

  @SuppressWarnings("method.invocation.invalid")
  public UserFormattingHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (!event.sender().hasPermission("carbonchat.formatting") &&
        !event.sender().hasPermission("carbonchat.channels." + event.channel().key() + ".formatting")) {
        this.suppressFormatting(event);
      }
      // TODO: convert ALL color code formats
    });
  }

  private void suppressFormatting(final PreChatFormatEvent event) {
    event.message(MiniMessage.get().escapeTokens(event.message()));
  }

}
