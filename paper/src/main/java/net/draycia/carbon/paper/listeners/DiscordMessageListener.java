/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.paper.listeners;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.essentialsx.api.v2.events.discord.DiscordMessageEvent;
import net.essentialsx.api.v2.events.discord.DiscordRelayEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.MessageType;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DiscordMessageListener implements Listener {

    private final CarbonChat carbonChat;
    private final JavaPlugin plugin;
    private final Map<Key, MessageType> channelMessageTypes = new HashMap<>();

    @Inject
    private DiscordMessageListener(
        final JavaPlugin plugin,
        final CarbonChat carbonChat,
        final Logger logger
    ) {
        this.plugin = plugin;
        this.carbonChat = carbonChat;
        logger.info("EssentialsXDiscord found! Enabling hook.");
    }

    // Minecraft -> Discord
    @EventHandler
    public void onDiscordMessage(final DiscordMessageEvent event) {
        if (!event.getType().equals(MessageType.DefaultTypes.CHAT)) {
            return;
        }

        final CarbonPlayer result = this.carbonChat.userManager().user(event.getUUID()).join();

        final ChatChannel channel = result.selectedChannel();

        if (channel == null) {
            return;
        }

        final MessageType messageType = this.channelMessageTypes.get(channel.key());

        event.setType(messageType);
    }

    @EventHandler
    public void onDiscordMessage(final DiscordRelayEvent event) {
    }

    public void init() {
        final @Nullable DiscordService discord = Bukkit.getServicesManager().load(DiscordService.class);

        if (discord != null) {
            this.carbonChat.channelRegistry().allKeys(key -> {
                final MessageType channelMessageType = new MessageType(key.value());
                discord.registerMessageType(this.plugin, channelMessageType);
                this.channelMessageTypes.put(key, channelMessageType);
            });
        }
    }

}
