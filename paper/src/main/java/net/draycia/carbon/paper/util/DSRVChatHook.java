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
package net.draycia.carbon.paper.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage;
import github.scarsz.discordsrv.hooks.chat.ChatHook;
import github.scarsz.discordsrv.util.PluginUtil;
import java.time.Duration;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.util.RenderedMessage;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.util.ChannelUtils;
import net.draycia.carbon.common.util.DiscordRecipient;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import static github.scarsz.discordsrv.util.MessageUtil.DEFAULT_URL_PATTERN;
import static net.draycia.carbon.api.util.KeyedRenderer.keyedRenderer;
import static net.kyori.adventure.key.Key.key;

public class DSRVChatHook implements ChatHook {

    public DSRVChatHook() {
        final Cache<ImmutablePair<CarbonPlayer, ChatChannel>, Component> awaitingEvent = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMillis(25))
            .build();

        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, event -> {
            if (event.previewing()) {
                final var pair = new ImmutablePair<>(event.sender(), event.chatChannel());
                awaitingEvent.put(pair, event.message());
                return;
            }

            final ChatChannel chatChannel = event.chatChannel();
            final CarbonPlayer carbonPlayer = event.sender();
            final ImmutablePair<CarbonPlayer, ChatChannel> pair = new ImmutablePair<>(carbonPlayer, chatChannel);
            Component messageComponent = awaitingEvent.getIfPresent(pair);
            awaitingEvent.invalidate(pair);

            if (messageComponent == null) {
                messageComponent = event.message();
            }

            var renderedMessage = new RenderedMessage(messageComponent, MessageType.CHAT);
            renderedMessage = keyedRenderer(key("carbon", "discord"), chatChannel).render(carbonPlayer, DiscordRecipient.INSTANCE, renderedMessage.component(), renderedMessage.component(), chatChannel);
            // TODO: Should we bother with any of these renders?
            for (final var renderer : event.renderers()) {
                if (renderer.key().asString().equals("carbon:default")) continue;
                renderedMessage = renderer.render(carbonPlayer, carbonPlayer, renderedMessage.component(), renderedMessage.component(), chatChannel);
            }

            final var messageContents = PlainTextComponentSerializer.plainText().serialize(renderedMessage.component());
            Component eventMessage = ConfigChatChannel.parseMessageTags(carbonPlayer, messageContents);

            if (carbonPlayer.hasPermission("carbon.chatlinks")) {
                eventMessage = eventMessage.replaceText(TextReplacementConfig.builder()
                    .match(DEFAULT_URL_PATTERN)
                    .replacement(builder -> builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, builder.content())))
                    .build());
            }

            DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Received a CarbonChatEvent (player: " + carbonPlayer.username() + ")");

            final @Nullable Player player = ((CarbonPlayerPaper) carbonPlayer).bukkitPlayer();
            final String message = PlainTextComponentSerializer.plainText().serialize(eventMessage);

            if (player != null) {
                DiscordSRV.getPlugin().processChatMessage(player, message, chatChannel.commandName(), event.result().cancelled());
            }
        });

        DiscordSRV.api.subscribe(new Object() {
            @Subscribe
            public void handle(final GameChatMessagePreProcessEvent event) {
                if (event.getTriggeringBukkitEvent() == null) return;
                event.setCancelled(true);
            }
        });
    }

    @Override
    public void broadcastMessageToChannel(final String channel, final github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component message) {
        final String mmFormattedMessage = MiniMessage.miniMessage().serialize(message);
        ChannelUtils.broadcastMessageToChannel(mmFormattedMessage, ChannelUtils.locateChannel(channel));
    }

    @Override
    public Plugin getPlugin() {
        return PluginUtil.getPlugin("CarbonChat");
    }

}
