package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.velocity.CarbonChatVelocity;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

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
        if (!event.getResult().isAllowed()) {
            return;
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());

        final var playerResult = this.carbonChat.server().player(event.getPlayer().getUniqueId()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());

        final var originalMessage = event.getMessage();

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (originalMessage.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                break;
            }
        }

        // TODO: option to specify if the channel should invoke ChatChannel#recipients
        //   or ChatChannel#filterRecipients
        //   for now we will just always invoke ChatChannel#recipients
        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, Component.text(event.getMessage()), recipients, renderers, channel);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        for (final var recipient : chatEvent.recipients()) {
            var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

            for (final var renderer : chatEvent.renderers()) {
                try {
                    if (recipient instanceof Player player) {
                        final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(player).join();

                        renderedMessage = renderer.render(sender, targetPlayer.player(), renderedMessage.component(), chatEvent.message());
                    } else {
                        renderedMessage = renderer.render(sender, recipient, renderedMessage.component(), chatEvent.message());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            final Identity identity;

            if (sender.hasPermission("carbon.hideidentity")) {
                identity = Identity.nil();
            } else {
                identity = sender.identity();
            }

            recipient.sendMessage(identity, renderedMessage.component(), renderedMessage.messageType());
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

}
