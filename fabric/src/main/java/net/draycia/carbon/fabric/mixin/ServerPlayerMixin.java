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

import net.draycia.carbon.fabric.callback.PlayerStatusMessageEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceKey;)V"
        )
    )
    public void redirectBroadcastDeath(final PlayerList instance, final Component component, final ResourceKey<ChatType> resourceKey) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastSystemMessage(msg, resourceKey);
        }
    }

    @Redirect(
        method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
        )
    )
    public void redirectBroadcastDeathToTeam(final PlayerList instance, final Player player, final Component component) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastSystemToTeam(player, msg);
        }
    }

    @Redirect(
        method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemToAllExceptTeam(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/network/chat/Component;)V"
        )
    )
    public void redirectBroadcastDeathToAllExceptTeam(final PlayerList instance, final Player player, final Component component) {
        final @Nullable Component msg = this.carbon$fireDeathMessageEvent(component);
        if (msg != null) {
            instance.broadcastSystemToAllExceptTeam(player, msg);
        }
    }

    private @Nullable Component carbon$fireDeathMessageEvent(final Component message) {
        final PlayerStatusMessageEvents.MessageEvent event = PlayerStatusMessageEvents.MessageEvent.of(
            (ServerPlayer) (Object) this, message.asComponent()
        );
        PlayerStatusMessageEvents.DEATH_MESSAGE.invoker().onMessage(event);
        final net.kyori.adventure.text.@Nullable Component msg = event.message();
        if (msg == null) {
            return null;
        }
        return FabricServerAudiences.of(this.server).toNative(msg);
    }

}
