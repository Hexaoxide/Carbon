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
package net.draycia.carbon.paper.hooks;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage;
import github.scarsz.discordsrv.hooks.chat.ChatHook;
import java.time.Duration;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.util.ChannelUtils;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DSRVChatHook implements ChatHook {

    private final CarbonChannelRegistry channelRegistry;
    private final JavaPlugin plugin;

    @Inject
    private DSRVChatHook(
        final CarbonEventHandler events,
        final CarbonChannelRegistry channelRegistry,
        final JavaPlugin plugin
    ) {
        this.channelRegistry = channelRegistry;
        this.plugin = plugin;

        final Cache<ImmutablePair<CarbonPlayer, ChatChannel>, Component> awaitingEvent = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMillis(25))
            .build();

        events.subscribe(CarbonChatEvent.class, event -> {
            final ChatChannel chatChannel = event.chatChannel();
            final CarbonPlayer carbonPlayer = event.sender();
            final ImmutablePair<CarbonPlayer, ChatChannel> pair = new ImmutablePair<>(carbonPlayer, chatChannel);
            Component messageComponent = awaitingEvent.getIfPresent(pair);
            awaitingEvent.invalidate(pair);

            if (messageComponent == null) {
                messageComponent = event.message();
            }

            final String messageContents = PlainTextComponentSerializer.plainText().serialize(messageComponent);
            final Component parsedMessage = ConfigChatChannel.parseMessageTags(carbonPlayer, messageContents);

            DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Received a CarbonChatEvent (player: " + carbonPlayer.username() + ")");

            final @Nullable Player player = ((CarbonPlayerPaper) carbonPlayer).bukkitPlayer();
            final String message = PlainTextComponentSerializer.plainText().serialize(parsedMessage);

            if (player != null) {
                DiscordSRV.getPlugin().processChatMessage(player, message, chatChannel.commandName(), event.cancelled());
            }
        });

        DiscordSRV.api.subscribe(new Object() {
            @Subscribe
            public void handle(final GameChatMessagePreProcessEvent event) {
                if (event.getTriggeringBukkitEvent() == null) {
                    return;
                }

                event.setCancelled(true);
            }
        });
    }

    @Override
    public void broadcastMessageToChannel(final String channel, final github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component message) {
        final String mmFormattedMessage = MiniMessage.miniMessage().serialize(message);
        final ChatChannel chatChannel = this.channelRegistry.byCommandName(channel);

        if (chatChannel == null) {
            this.plugin.getLogger().warning("Error sending message from Discord to Minecraft, no matching channel found for [" + channel + "]");
        } else {
            ChannelUtils.broadcastMessageToChannel(mmFormattedMessage, chatChannel);
        }
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

}
