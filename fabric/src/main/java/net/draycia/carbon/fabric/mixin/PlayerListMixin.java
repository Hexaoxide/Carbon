package net.draycia.carbon.fabric.mixin;

import cloud.commandframework.types.tuples.Triplet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class PlayerListMixin {

    @Shadow @Final private MinecraftServer server;

    @Shadow
    public abstract void broadcastMessage(Component component, ChatType chatType, UUID uUID);

    public Map<Thread, Triplet<Component, ChatType, UUID>> carbon$joinMsg = new ConcurrentHashMap<>();

    @Redirect(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
        )
    )
    public void redirectJoinMessage(final PlayerList instance, final Component component, final ChatType chatType, final UUID uuid) {
        // move to after player is added to playerlist and world
        this.carbon$joinMsg.put(Thread.currentThread(), Triplet.of(component, chatType, uuid));
    }

    @Inject(
        method = "placeNewPlayer",
        at = @At("RETURN")
    )
    public void injectJoin(final Connection connection, final ServerPlayer serverPlayer, final CallbackInfo ci) {
        final @Nullable Triplet<Component, ChatType, UUID> remove = this.carbon$joinMsg.remove(Thread.currentThread());
        if (remove != null) {
            final FabricServerAudiences audiences = FabricServerAudiences.of(this.server);
            final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
                serverPlayer, audiences.toAdventure(remove.getFirst())
            );
            PlayerStatusMessageEvents.JOIN_MESSAGE.invoker().onMessage(event);
            final net.kyori.adventure.text.@Nullable Component message = event.message();
            if (message != null) {
                this.broadcastMessage(audiences.toNative(message), remove.getSecond(), remove.getThird());
            }
        }
    }

}
