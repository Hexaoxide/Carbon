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
    var original = event.originalChatRouter().chat(sender, event.originalMessage());



    // annoying bullshit, can't get recipients at all, can't format per-player
    // cancel event and do EVERYTHING ourselves...
    // there was discussion about this issue 6 YEARS AGO
    // there have been pending PRs to address this blatant issue for ///6 years///
    // https://github.com/SpongePowered/SpongeAPI/issues/612
    // https://github.com/SpongePowered/SpongeAPI/pull/2340
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
