package net.draycia.carbon.listeners;

import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatComponentEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.event.PostOrders;

import java.util.regex.Pattern;

public class UrlLinkHandler {

  private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

  public UrlLinkHandler() {
    CarbonEvents.register(ChatComponentEvent.class, PostOrders.NORMAL, true, event -> {
      final TextComponent newComponent = (TextComponent) event.component()
        .replaceText(URL_PATTERN, url -> url.clickEvent(ClickEvent.openUrl(url.content())));

      event.component(newComponent);
    });
  }

}
