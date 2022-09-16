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
package net.draycia.carbon.fabric.listeners;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.KeyedRenderer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.config.ConfigFactory;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNullElse;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class FabricChatPreviewListener implements ChatDecorator {

    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

    private final ConfigFactory configFactory;
    private final CarbonChatFabric carbonChat;
    private final ChannelRegistry channelRegistry;

    @Inject
    public FabricChatPreviewListener(
        final ConfigFactory configFactory,
        final CarbonChatFabric carbonChat,
        final ChannelRegistry channelRegistry
    ) {
        this.configFactory = configFactory;
        this.carbonChat = carbonChat;
        this.channelRegistry = channelRegistry;
    }

    @Override
    public CompletableFuture<Component> decorate(final @Nullable ServerPlayer serverPlayer, final Component component) {
        if (serverPlayer == null) {
            return CompletableFuture.completedFuture(component);
        }

        final var playerResult = this.carbonChat.server().userManager().carbonPlayer(serverPlayer.getUUID()).join();
        final @Nullable CarbonPlayer sender = playerResult.player();

        if (sender == null) {
            return CompletableFuture.completedFuture(component);
        }

        var channel = requireNonNullElse(sender.selectedChannel(), this.channelRegistry.defaultValue());

        String content = component.getString();

        for (final Map.Entry<String, String> placeholder : this.configFactory.primaryConfig().chatPlaceholders().entrySet()) {
            content = content.replace(placeholder.getKey(), placeholder.getValue());
        }

        net.kyori.adventure.text.Component message = MiniMessage.miniMessage().deserialize(content);

        if (sender.hasPermission("carbon.chatlinks")) {
            message = message.replaceText(TextReplacementConfig.builder()
                .match(DEFAULT_URL_PATTERN)
                .replacement(builder -> builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, builder.content())))
                .build());
        }

        for (final var chatChannel : this.channelRegistry) {
            if (chatChannel.quickPrefix() == null) {
                continue;
            }

            if (content.startsWith(chatChannel.quickPrefix()) && chatChannel.speechPermitted(sender).permitted()) {
                channel = chatChannel;
                message = message.replaceText(TextReplacementConfig.builder()
                    .once()
                    .matchLiteral(channel.quickPrefix())
                    .replacement(text())
                    .build());
                break;
            }
        }

        final var renderers = new ArrayList<KeyedRenderer>();
        renderers.add(keyedRenderer(key("carbon", "default"), channel));

        final var chatEvent = new CarbonChatEvent(sender, message, this.carbonChat.server().players(), renderers, channel, true);
        final var result = this.carbonChat.eventHandler().emit(chatEvent);

        if (!result.wasSuccessful()) {
            final var failure = chatEvent.result().reason();

            if (!failure.equals(empty())) {
                sender.sendMessage(failure);
            }

            return CompletableFuture.completedFuture(component);
        }

        // start here
        var renderedMessage = new RenderedMessage(chatEvent.message(), MessageType.CHAT);

        for (final var renderer : chatEvent.renderers()) {
            renderedMessage = renderer.render(sender, sender, renderedMessage.component(), chatEvent.message());
        }

        final Component mojangComponent = FabricAudiences.nonWrappingSerializer().serialize(renderedMessage.component());

        return CompletableFuture.completedFuture(mojangComponent);
        // TODO: recipients?
        // TODO: per-player formatting?
    }

}
