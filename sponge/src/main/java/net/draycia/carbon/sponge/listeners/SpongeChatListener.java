package net.draycia.carbon.sponge.listeners;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

import java.util.ArrayList;

import static net.draycia.carbon.common.Injector.byInject;

public final class SpongeChatListener {

  private final CarbonChat carbonChat = byInject(CarbonChat.class);
  private final UserManager userManager = this.carbonChat.userManager();

  @Listener
  public void onPlayerChat(final @NonNull PlayerChatEvent event, final @First Player sender) {
    // https://github.com/SpongePowered/SpongeAPI/pull/2340
    // this event currently doesn't have a concept of recipients, it seems?
    // or they're in the stack?
    // idk, let's just do our own thing in the meantime
    event.setCancelled(true);

    final var player = this.userManager.carbonPlayer(sender.uniqueId());

    if (player == null) {
      return;
    }

    final var recipients = new ArrayList<CarbonPlayer>();

    for (final ServerPlayer recipient : Sponge.server().onlinePlayers()) {
      final var bukkitRecipient = this.userManager.carbonPlayer(recipient.uniqueId());

      if (bukkitRecipient != null) {
        recipients.add(bukkitRecipient);
      }
    }

    final var chatEvent = new CarbonChatEvent(player, event.message(), recipients);
    final var result = this.carbonChat.eventHandler().emit(chatEvent);

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
