package net.draycia.carbon.sponge.listeners;

import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

import java.util.ArrayList;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.draycia.carbon.common.Injector.byInject;
import static net.kyori.adventure.key.Key.key;

public final class SpongeChatListener {

    private final CarbonChatSponge carbonChat = byInject(CarbonChat.class); // lol this is dumb
    private final UserManager userManager = this.carbonChat.userManager();

    @Listener
    public void onPlayerChat(final @NonNull PlayerChatEvent event, final @First Player messageSender) {
        // https://github.com/SpongePowered/SpongeAPI/pull/2340
        // this event currently doesn't have a concept of recipients, it seems?
        // or they're in the stack?
        // idk, let's just do our own thing in the meantime
        event.setCancelled(true);

        final var sender = this.userManager.carbonPlayer(messageSender.uniqueId());

        if (sender == null) {
            return;
        }

        final var recipients = new ArrayList<Audience>();
        final var channel = sender.selectedChannel();

        for (final ServerPlayer spongeRecipient : Sponge.server().onlinePlayers()) {
            final var recipient = this.userManager.carbonPlayer(spongeRecipient.uniqueId());

            if (recipient != null && channel.mayReceiveMessages(recipient)) {
                recipients.add(recipient);
            }
        }

        // console too!
        // i'm not 100% sure this is "console" but I literally cannot find anything that explicitly says console
        recipients.add(Sponge.systemSubject());

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
        }
    }

}
