package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.common.channels.BasicChatChannel;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public final class VelocityChatListener {

    private final CarbonChat carbonChat;
    private final BasicChatChannel basicChat;

    @Inject
    public VelocityChatListener(final CarbonChat carbonChat, final BasicChatChannel basicChat) {
        this.carbonChat = carbonChat;
        this.basicChat = basicChat;
    }

    @Subscribe
    public void onPlayerChat(final @NonNull PlayerChatEvent event) {
        final var sender = this.carbonChat.server().player(event.getPlayer().getUniqueId());

        if (sender == null) {
            return;
        }

        final var recipients = new ArrayList<Audience>();
        var channel = sender.selectedChannel();

        if (channel == null) {
            channel = this.basicChat;
        }

        for (final CarbonPlayer player : this.carbonChat.server().players()) {
            if (channel.hearingPermitted(player).permitted()) {
                recipients.add(player);
            }
        }

        // console too!
        recipients.add(this.carbonChat.server());

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, text(event.getMessage()), recipients, renderers);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (result.wasSuccessful()) {
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

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

}
