package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.velocity.CarbonChatVelocity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class VelocityChatListener {

    private final CarbonChatVelocity carbonChat;
    private final ChannelRegistry registry;

    @Inject
    private VelocityChatListener(final CarbonChat carbonChat, final ChannelRegistry registry) {
        this.carbonChat = (CarbonChatVelocity) carbonChat;
        this.registry = registry;
    }

    @Subscribe
    public void onPlayerChat(final PlayerChatEvent event) {
        final var playerResult = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();

        if (playerResult.player() == null) {
            return;
        }

        final var sender = playerResult.player();

        final var channel = requireNonNullElse(playerResult.player().selectedChannel(),
            this.registry.defaultValue());

        final var recipients = channel.recipients(sender);

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

                    if (recipient instanceof Player player) {
                        final ComponentPlayerResult targetPlayer = this.carbonChat.server().player(player).join();
                        component = renderer.render(playerResult.player(), targetPlayer.player(), component, chatEvent.message());
                    } else {
                        component = renderer.render(playerResult.player(), recipient, component, chatEvent.message());
                    }
                }

                if (component != Component.empty()) {
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
