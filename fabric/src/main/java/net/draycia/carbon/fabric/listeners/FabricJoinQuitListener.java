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
import com.google.inject.Provider;
import java.util.List;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.users.ProfileCache;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.util.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public class FabricJoinQuitListener implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect {

    private final ProfileCache profileCache;
    private final Logger logger;
    private final ConfigManager configManager;
    private final UserManagerInternal<?> userManager;
    private final Provider<MessagingManager> messaging;
    private final PacketFactory packetFactory;

    @Inject
    public FabricJoinQuitListener(
        final Logger logger,
        final ConfigManager configManager,
        final ProfileCache profileCache,
        final UserManagerInternal<?> userManager,
        final Provider<MessagingManager> messaging,
        final PacketFactory packetFactory
    ) {
        this.logger = logger;
        this.configManager = configManager;
        this.profileCache = profileCache;
        this.userManager = userManager;
        this.messaging = messaging;
        this.packetFactory = packetFactory;
    }

    @Override
    public void onPlayReady(final ServerGamePacketListenerImpl handler, final PacketSender sender, final MinecraftServer server) {
        this.profileCache.cache(handler.getPlayer().getUUID(), handler.getPlayer().getGameProfile().getName());
        this.messaging.get().withPacketService(packetService -> {
            packetService.queuePacket(this.packetFactory.addLocalPlayerPacket(handler.getPlayer().getUUID(), handler.getPlayer().getGameProfile().getName()));
        });

        final @Nullable List<String> suggestions = this.configManager.primaryConfig().customChatSuggestions();

        if (suggestions == null || suggestions.isEmpty()) {
            return;
        }

        sender.sendPacket(new ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action.SET, suggestions));
    }

    @Override
    public void onPlayDisconnect(final ServerGamePacketListenerImpl handler, final MinecraftServer server) {
        this.userManager.loggedOut(handler.getPlayer().getGameProfile().getId())
            .exceptionally(saveExceptionHandler(this.logger, handler.getPlayer().getGameProfile().getName(), handler.getPlayer().getGameProfile().getId()));
    }

}
