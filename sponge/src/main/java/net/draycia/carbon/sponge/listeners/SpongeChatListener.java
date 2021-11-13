package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Optional;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Tristate;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;

@DefaultQualifier(NonNull.class)
public final class SpongeChatListener {

    private final CarbonChatSponge carbonChat;
    private final ChannelRegistry registry;

    @Inject
    private SpongeChatListener(
        final CarbonChat carbonChat,
        final ChannelRegistry registry
    ) {
        this.carbonChat = (CarbonChatSponge) carbonChat;
        this.registry = registry;
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onPlayerChat(final PlayerChatEvent event, final @First Player source) {
        final var playerResult = this.carbonChat.server().player(source.uniqueId()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());

        final var messageContents = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (messageContents.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
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

        final var chatEvent = new CarbonChatEvent(sender, event.message(), recipients, renderers, channel);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        try {
            event.setAudience(Audience.audience(chatEvent.recipients()));
        } catch (final UnsupportedOperationException exception) {
            exception.printStackTrace();
            // Do we log something here? Would get spammy fast.
        }

        if (sender.hasPermission("carbon.hideidentity")) {
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

                recipient.sendMessage(Identity.nil(), renderedMessage.component(), renderedMessage.messageType());
            }
        } else {
            event.setChatFormatter((player, target, message, originalMessage) -> {
                Component component = message;

                for (final var renderer : chatEvent.renderers()) {
                    if (target instanceof ServerPlayer serverPlayer) {
                        final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(serverPlayer).join();
                        component = renderer.render(playerResult.player(), targetPlayer.player(), component, message).component();
                    } else {
                        component = renderer.render(playerResult.player(), target, component, message).component();
                    }
                }

                if (component == Component.empty()) {
                    return Optional.empty();
                }

                return Optional.ofNullable(component);
            });
        }
    }

}
