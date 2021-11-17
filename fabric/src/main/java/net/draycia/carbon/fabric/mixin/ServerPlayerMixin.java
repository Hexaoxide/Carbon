package net.draycia.carbon.fabric.mixin;

import java.util.UUID;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {

    @Shadow @Final public MinecraftServer server;

    @Redirect(
        method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"
        )
    )
    public void redirectBroadcastDeath(final PlayerList instance, final Component component, final ChatType chatType, final UUID uuid) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastMessage(msg, chatType, uuid);
        }
    }

    @Redirect(
        method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastToTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
        )
    )
    public void redirectBroadcastDeathToTeam(final PlayerList instance, final Player player, final Component component) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastToTeam(player, msg);
        }
    }

    @Redirect(
        method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastToAllExceptTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
        )
    )
    public void redirectBroadcastDeathToAllExceptTeam(final PlayerList instance, final Player player, final Component component) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastToAllExceptTeam(player, msg);
        }
    }

    private @Nullable Component carbon$fireDeathMessageEvent(final Component message) {
        final FabricServerAudiences audiences = FabricServerAudiences.of(this.server);
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            (ServerPlayer) (Object) this, audiences.toAdventure(message)
        );
        PlayerStatusMessageEvents.DEATH_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component msg = event.message();
        if (msg == null) {
            return null;
        }
        return audiences.toNative(msg);
    }

}
