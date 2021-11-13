package net.draycia.carbon.fabric.mixin;

import net.draycia.carbon.fabric.callback.FabricChatCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    protected abstract void handleCommand(String string);

    @Shadow @Final private MinecraftServer server;

    @Shadow public ServerPlayer player;

    @Redirect(
        method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
        at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z")
    )
    public boolean redirectStartsWith(final String string, final String prefix) {
        return true;
    }

    @Redirect(
        method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;handleCommand(Ljava/lang/String;)V")
    )
    public void redirectHandleCommand(final ServerGamePacketListenerImpl serverGamePacketListener, final String string) {
        if (string.startsWith("/")) {
            this.handleCommand(string);
            return;
        }
        FabricChatCallback.INSTANCE.fireAsync(this.server, this.player, string);
    }

}
