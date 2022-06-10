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
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            this.player, component.asComponent()
        );
        PlayerStatusMessageEvents.QUIT_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component message = event.message();
        if (message != null) {
            instance.broadcastMessage(FabricServerAudiences.of(this.server).toNative(message), chatType, uuid);
        }
    }

}
