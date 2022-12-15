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

import net.draycia.carbon.fabric.CarbonChatFabric;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatType.class)
public abstract class ChatTypeMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(final BootstapContext<ChatType> context, final CallbackInfo info) {
        context.register(CarbonChatFabric.CHAT_TYPE,
            new ChatType(ChatTypeDecoration.withSender("%s"), ChatTypeDecoration.withSender("%s")));
    }

}
