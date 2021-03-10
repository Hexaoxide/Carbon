package net.draycia.carbon.common.listeners;

import net.draycia.carbon.api.events.ChatFormatEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.kyori.event.PostOrders;

import java.util.regex.Pattern;

public class UrlLinkHandler {

  private static final Pattern URL_PATTERN = Pattern.compile("((?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?)");

  public UrlLinkHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.NORMAL, true, event -> {
      if (event.sender().hasPermission("carbonchat.autourl")) {
        event.message(URL_PATTERN.matcher(event.message()).replaceAll("<click:open_url:'$1'>$1</click>"));
        // Reminder: Do not use this on event.format(), this breaks hover events when they contain URLs
      }
    });
  }

}
