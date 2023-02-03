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

import com.google.inject.Inject;
import java.util.List;
import net.draycia.carbon.common.config.ConfigFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class FabricPlayerJoinListener implements ServerPlayConnectionEvents.Join {

    private ConfigFactory configFactory;

    @Inject
    public FabricPlayerJoinListener(final ConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    @Override
    public void onPlayReady(final ServerGamePacketListenerImpl handler, final PacketSender sender, final MinecraftServer server) {
        final @Nullable List<String> suggestions = this.configFactory.primaryConfig().customChatSuggestions();

        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }

        sender.sendPacket(new ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action.SET,
            suggestions));
    }

}
