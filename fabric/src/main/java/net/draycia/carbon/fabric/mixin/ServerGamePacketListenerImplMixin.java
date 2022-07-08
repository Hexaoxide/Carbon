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

import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {

    @Shadow @Final private MinecraftServer server;

    @Shadow public ServerPlayer player;

    @Inject(
        method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Lnet/minecraft/server/network/FilteredText;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ServerboundChatPacket;getSignature(Ljava/util/UUID;)Lnet/minecraft/network/chat/MessageSignature;",
            shift = At.Shift.BEFORE
        )
        // , cancellable = true // todo
    )
    public void injectHandleChat(final ServerboundChatPacket serverboundChatPacket, final FilteredText<String> filteredText, final CallbackInfo ci) {
        // todo
    }

    @Inject(
        method = "queryChatPreview",
        at = @At("HEAD")
        // , cancellable = true // todo
    )
    public void injectQueryChatPreview(final String string, final CallbackInfoReturnable<CompletableFuture<Component>> cir) {
        // todo
    }

    @Redirect(
        method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceKey;)V"
        )
    )
    public void redirectQuitMessage(final PlayerList instance, final Component component, final ResourceKey<ChatType> resourceKey) {
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            this.player, component.asComponent()
        );
        PlayerStatusMessageEvents.QUIT_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component message = event.message();
        if (message != null) {
            instance.broadcastSystemMessage(FabricServerAudiences.of(this.server).toNative(message), resourceKey);
        }
    }

}
