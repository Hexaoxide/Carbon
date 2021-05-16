package net.draycia.carbon.bukkit.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.bukkit.CarbonChatBukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Map;

import static net.draycia.carbon.common.Injector.byInject;
import static net.kyori.adventure.key.Key.key;

public final class BukkitChatListener implements Listener {

  private final CarbonChatBukkit carbonChat = byInject(CarbonChat.class); // lol this is dumb
  private final UserManager userManager = this.carbonChat.userManager();

  @EventHandler
  public void onPlayerChat(final @NonNull AsyncChatEvent event) {
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

    final var chatEvent = new CarbonChatEvent(player, event.message(), recipients,
      Map.of(key("carbon", "default"), this.carbonChat.vanillaChatRenderer));
    final var result = this.carbonChat.eventHandler().emit(chatEvent);

    if (result.wasSuccessful()) {
      // TODO: send to channels

      for (final var recipient : chatEvent.recipients()) {
        var component = chatEvent.message();

        for (final var entry : chatEvent.renderers().entrySet()) {
          component = entry.getValue().render(player,
            recipient, chatEvent.message(), chatEvent.originalMessage());
        }

        if (component != null) {
          recipient.sendMessage(component);
        }
      }
    }

    // There's no guarantee that recipients is mutable.
    // If it's ever immutable, we yell at who ever is responsible.
    event.recipients().clear();
  }

}
