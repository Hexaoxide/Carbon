package net.draycia.carbon.listeners;

import net.draycia.carbon.events.ChatComponentEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

public class UrlLinkHandler implements Listener {

  private static final Pattern URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onUrlLink(final ChatComponentEvent event) {
    final TextComponent newComponent = (TextComponent) event.component()
      .replaceText(URL_PATTERN, url -> url.clickEvent(ClickEvent.openUrl(url.content())));

    event.component(newComponent);
  }

}
