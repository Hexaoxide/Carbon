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

import github.scarsz.discordsrv.Debug;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage;
import github.scarsz.discordsrv.hooks.chat.ChatHook;
import github.scarsz.discordsrv.util.PluginUtil;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.events.CarbonChatEvent;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.util.ChannelUtils;
import net.draycia.carbon.paper.users.CarbonPlayerPaper;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CarbonChatHook implements ChatHook {

    public CarbonChatHook() {
        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent.class, event -> {
            final ChatChannel chatChannel = event.chatChannel();
            final CarbonPlayer carbonPlayer = event.sender();

            DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Received a CarbonChatEvent (player: " + carbonPlayer.username() + ")");

            final @Nullable Player player = ((CarbonPlayerPaper) carbonPlayer).bukkitPlayer();
            final String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            if (player != null) {
                DiscordSRV.getPlugin().processChatMessage(player, message, chatChannel.commandName(), event.result().cancelled());
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
