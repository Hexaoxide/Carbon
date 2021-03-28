package net.draycia.carbon.bukkit.listeners;

import net.draycia.carbon.api.events.UserEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BukkitLoginHandler implements Listener {

  private final @NonNull CarbonChatBukkit carbonChat;

  public BukkitLoginHandler(final @NonNull CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;
  }

  @SuppressWarnings("argument.type.incompatible")
  @EventHandler
  public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
    this.carbonChat.cacheUUID(event.getPlayer().getName(), event.getPlayer().getUniqueId());

    this.carbonChat.userService().wrapLater(event.getPlayer().getUniqueId()).thenAcceptAsync(user -> {
      final UserEvent.Join joinEvent = new UserEvent.Join(user);

      CarbonEvents.post(joinEvent);
    });
  }

  @EventHandler
  public void onPlayerLeave(final @NonNull PlayerQuitEvent event) {
    this.carbonChat.userService().wrapLater(event.getPlayer().getUniqueId()).thenAcceptAsync(user -> {
      final UserEvent.Leave leaveEvent = new UserEvent.Leave(user);

      CarbonEvents.post(leaveEvent);
    });
  }

}
