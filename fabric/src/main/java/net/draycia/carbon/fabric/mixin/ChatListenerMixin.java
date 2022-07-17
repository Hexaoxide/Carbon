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
package net.draycia.carbon.fabric.mixin;

import java.util.UUID;
import net.draycia.carbon.fabric.CarbonChatFabric;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ChatListenerMixin {

    @ModifyVariable(at = @At("HEAD"), index = 1, method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", argsOnly = true)
    private Packet<?> sendPacket(final Packet<?> packet) {
        if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
            final UUID uuid = UUID.nameUUIDFromBytes(chatPacket.message().headerSignature().bytes());
            CarbonChatFabric.addMessageSignature(uuid, chatPacket.message().headerSignature());
        }

        return packet;
    }

}
