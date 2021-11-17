package net.draycia.carbon.fabric.mixin;

import java.util.UUID;
import net.draycia.carbon.fabric.callback.ChatCallback;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
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
abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    protected abstract void handleCommand(String string);

    @Shadow @Final private MinecraftServer server;

    @Shadow public ServerPlayer player;

    @Redirect(
        method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"
        )
    )
    public boolean redirectStartsWith(final String string, final String prefix) {
        return true;
    }

    @Redirect(
        method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;handleCommand(Ljava/lang/String;)V"
        )
    )
    public void redirectHandleCommand(final ServerGamePacketListenerImpl serverGamePacketListener, final String string) {
        if (string.startsWith("/")) {
            this.handleCommand(string);
            return;
        }
        ChatCallback.INSTANCE.fireAsync(this.server, this.player, string);
    }

    @Redirect(
        method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
        )
    )
    public void redirectQuitMessage(final PlayerList instance, final Component component, final ChatType chatType, final UUID uuid) {
        final FabricServerAudiences audiences = FabricServerAudiences.of(this.server);
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            this.player, audiences.toAdventure(component)
        );
        PlayerStatusMessageEvents.QUIT_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component message = event.message();
        if (message != null) {
            instance.broadcastMessage(audiences.toNative(message), chatType, uuid);
        }
    }

}
