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
    private Packet<?> sendPacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
            final UUID uuid = UUID.nameUUIDFromBytes(chatPacket.message().headerSignature().bytes());
            CarbonChatFabric.addMessageSignature(uuid, chatPacket.message().headerSignature());
        }

        return packet;
    }

}
