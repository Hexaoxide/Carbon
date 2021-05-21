package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

import java.util.ArrayList;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

public final class SpongeChatListener {

    @Inject private Game game;
    @Inject
    private CarbonChatSponge carbonChat;

    @Listener
    public void onPlayerChat(final @NonNull PlayerChatEvent event, final @First Player messageSender) {
        // https://github.com/SpongePowered/SpongeAPI/pull/2340
        // this event currently doesn't have a concept of recipients, it seems?
        // or they're in the stack?
        // idk, let's just do our own thing in the meantime
        event.setCancelled(true);

        final var sender = this.carbonChat.server().player(messageSender.uniqueId());

        if (sender == null) {
            return;
        }

        final var recipients = new ArrayList<Audience>();
        final var channel = sender.selectedChannel();

        for (final ServerPlayer spongeRecipient : this.game.server().onlinePlayers()) {
            final var recipient = this.carbonChat.server().player(spongeRecipient.uniqueId());

            if (recipient != null && channel.hearingPermitted(recipient).permitted()) {
                recipients.add(recipient);
            }
        }

        // console too!
        // i'm not 100% sure this is "console" but I literally cannot find anything that explicitly says console
        recipients.add(this.game.systemSubject());

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel.renderer()));

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients, renderers);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (result.wasSuccessful()) {
            // TODO: send to channels

            for (final var recipient : chatEvent.recipients()) {
                var component = chatEvent.message();

                for (final var renderer : chatEvent.renderers()) {
                    component = renderer.render(sender,
                        recipient, chatEvent.message(), chatEvent.originalMessage());
                }

                if (component != null) {
                    recipient.sendMessage(component);
                }
            }
        } else if (chatEvent.result().cancelled()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }
        }
    }

}
