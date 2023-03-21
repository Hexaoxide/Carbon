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
package net.draycia.carbon.fabric.mixin;

import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.draycia.carbon.fabric.chat.MessageRecipientFilter;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin implements ServerGamePacketListener {

    @Shadow @Final private MinecraftServer server;

    @Shadow public ServerPlayer player;

    @Redirect(
        method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    public void redirectQuitMessage(final PlayerList instance, final Component component, final boolean bool) {
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            this.player, component.asComponent()
        );
        PlayerStatusMessageEvents.QUIT_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component message = event.message();
        if (message != null) {
            instance.broadcastSystemMessage(FabricServerAudiences.of(this.server).toNative(message), bool);
        }
    }

    @Redirect(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"))
    private void broadcastChatMessage(final PlayerList instance, final PlayerChatMessage playerChatMessage, final ServerPlayer serverPlayer, final ChatType.Bound bound) {
        // TODO: Get the channel the message was sent in, when the player uses /channel <message>
        final CarbonPlayer result = CarbonChatProvider.carbonChat().server().userManager().user(serverPlayer.getUUID()).join();
        final MessageRecipientFilter filter = new MessageRecipientFilter(serverPlayer, result.selectedChannel());

        for (final ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            if (filter.shouldFilterMessageTo(player)) {
                // Chat headers not needed in 1.19.3+
                continue;
            }
            final OutgoingChatMessage outgoingPlayerChatMessage = new OutgoingChatMessage.Player(playerChatMessage);
            player.sendChatMessage(outgoingPlayerChatMessage, false, ChatType.bind(CarbonChatFabric.CHAT_TYPE, serverPlayer.level.registryAccess(), playerChatMessage.decoratedContent()));
        }
    }

}
