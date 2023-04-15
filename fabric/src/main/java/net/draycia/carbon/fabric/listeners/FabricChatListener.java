/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
package net.draycia.carbon.fabric.listeners;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.draycia.carbon.fabric.callback.ChatCallback;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.draycia.carbon.common.util.Strings.URL_REPLACEMENT_CONFIG;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public class FabricChatListener implements Consumer<ChatCallback.Chat> {

    private final CarbonChatFabric carbonChatFabric;
    private final ChannelRegistry channelRegistry;

    public FabricChatListener(final CarbonChatFabric carbonChatFabric, final ChannelRegistry channelRegistry) {
        this.carbonChatFabric = carbonChatFabric;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public void accept(final ChatCallback.Chat chat) {
        final @Nullable CarbonPlayer sender = this.carbonChatFabric.userManager().user(chat.sender().getUUID()).join();

        var channel = requireNonNullElse(sender.selectedChannel(), this.channelRegistry.defaultValue());
        final var originalMessage = chat.message();
        Component eventMessage = text(chat.message());

        if (sender.hasPermission("carbon.chatlinks")) {
            eventMessage = eventMessage.replaceText(URL_REPLACEMENT_CONFIG.get());
        }

        for (final var chatChannel : this.channelRegistry) {
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

        final var chatEvent = new CarbonChatEvent(sender, eventMessage, recipients, renderers, channel, null);
        final var result = this.carbonChatFabric.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var message = chatEvent.result().reason();

            if (!message.equals(empty())) {
                sender.sendMessage(message);
            }

            return;
        }

        if (sender.hasPermission("carbon.hideidentity")) {
            chat.identity(Identity.nil());
        }

        chat.formatter((sender1, message, viewer) -> {
            var renderedMessage = chatEvent.message();

            for (final var renderer : chatEvent.renderers()) {
                try {
                    final Optional<UUID> uuid = viewer.get(Identity.UUID);
                    if (uuid.isPresent()) {
                        final CarbonPlayer targetPlayer = this.carbonChatFabric.userManager().user(uuid.get()).join();

                        renderedMessage = renderer.render(sender, targetPlayer, renderedMessage, chatEvent.message());
                    } else {
                        renderedMessage = renderer.render(sender, viewer, renderedMessage, chatEvent.message());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            return renderedMessage;
        });
    }

}
