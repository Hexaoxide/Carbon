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
        method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
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
        method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
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
