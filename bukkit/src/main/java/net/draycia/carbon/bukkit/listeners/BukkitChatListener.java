package net.draycia.carbon.bukkit.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class BukkitChatListener implements Listener {

  private final CarbonChat carbonChat;
  private final UserManager userManager;

  public BukkitChatListener(final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;
    this.userManager = this.carbonChat.userManager();
  }

  @EventHandler
  public void onPlayerChat(final @NonNull AsyncChatEvent event) {
    // There's no guarantee that recipients is mutable.
    // If it's ever immutable, we yell at who ever is responsible.
    event.recipients().clear();

    final var player = this.userManager.carbonPlayer(event.getPlayer().getUniqueId());

    if (player == null) {
      return;
    }

    final var recipients = new ArrayList<CarbonPlayer>();

    for (final Player recipient : event.recipients()) {
      final var bukkitRecipient = this.userManager.carbonPlayer(recipient.getUniqueId());

      if (bukkitRecipient != null) {
        recipients.add(bukkitRecipient);
      }
    }

    final var chatEvent = new CarbonChatEvent(player, event.message(), recipients);
    final var result = this.carbonChat.eventHandler().post(chatEvent);

    if (result.wasSuccessful()) {
      // TODO: send to channels

      for (final var recipient : chatEvent.recipients()) {
        final var component = chatEvent.renderer()
          .render(player, recipient, chatEvent.message());

        if (component != null) {
          recipient.sendMessage(component);
        }
      }
    }
  }

}
