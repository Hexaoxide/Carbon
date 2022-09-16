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
package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;
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
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

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

        final ComponentPlayerResult<? extends CarbonPlayer> playerResult = this.carbonChat.server().userManager().carbonPlayer(event.getPlayer().getUniqueId()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return;
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.registry.defaultValue());

        final var originalMessage = event.getResult().getMessage().orElse(event.getMessage());
        Component eventMessage = text(originalMessage);

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

            if (originalMessage.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                    .replacement(text())
                    .build());
                break;
            }
        }

        final var recipients = channel.recipients(sender);

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel, false);
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
                renderedMessage = renderer.render(sender, recipient, renderedMessage.component(), chatEvent.message());
            }

            final Identity identity = sender.hasPermission("carbon.hideidentity") ? Identity.nil() : sender.identity();

            if (!(recipient instanceof CarbonPlayer)) {
                recipient.sendMessage(identity, renderedMessage.component());
            } else {
                recipient.sendMessage(identity, renderedMessage.component(), renderedMessage.messageType());
            }
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

}
