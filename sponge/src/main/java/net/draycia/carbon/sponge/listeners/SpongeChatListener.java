/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.sponge.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.sponge.CarbonChatSponge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
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
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class SpongeChatListener {

    private final CarbonChatSponge carbonChat;
    private final ChannelRegistry registry;

    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

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
        var eventMessage = event.message();

        if (sender.hasPermission("carbon.chatlinks")) {
            eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                .match(DEFAULT_URL_PATTERN)
                .replacement(builder -> builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, builder.content())))
                .build());
        }

        for (final var chatChannel : this.registry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (messageContents.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                        .replacement(text())
                    .build());
                break;
            }
        }

        final List<Audience> recipients;

        if (event.audience().isPresent()) {
            final var audience = event.audience().get();

            if (audience instanceof ForwardingAudience forwardingAudience) {
                recipients = new ArrayList<>();

                forwardingAudience.forEachAudience(recipients::add);
            } else {
                recipients = channel.recipients(sender);
            }
        } else {
            recipients = channel.recipients(sender);
        }

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel);
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
            event.setChatFormatter((player, target, msg, originalMessage) -> {
                Component component = msg;

                for (final var renderer : chatEvent.renderers()) {
                    if (target instanceof ServerPlayer serverPlayer) {
                        final ComponentPlayerResult<CarbonPlayer> targetPlayer = this.carbonChat.server().player(serverPlayer).join();
                        component = renderer.render(playerResult.player(), targetPlayer.player(), component, msg).component();
                    } else {
                        component = renderer.render(playerResult.player(), target, component, msg).component();
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
