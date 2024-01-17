/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.fabric.command;

import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.SignedGreedyStringParser;
import net.draycia.carbon.fabric.MinecraftServerHolder;
import net.draycia.carbon.fabric.listeners.FabricChatHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;

@DefaultQualifier(NonNull.class)
public final class FabricSignedMapper implements SignedGreedyStringParser.Mapper {

    private final MinecraftServerHolder serverHolder;

    @Inject
    private FabricSignedMapper(final MinecraftServerHolder serverHolder) {
        this.serverHolder = serverHolder;
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedGreedyStringParser.SignedString>> apply(
        final CommandContext<Commander> ctx,
        final String str
    ) {
        final CommandSourceStack stack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
        final Map<String, PlayerChatMessage> signedArgs;
        if (stack.getSigningContext() instanceof CommandSigningContext.SignedArguments) {
            signedArgs = ((CommandSigningContext.SignedArguments) stack.getSigningContext()).arguments();
        } else {
            return ArgumentParseResult.successFuture(
                new SignedGreedyStringParser.NonSignedString(str)
            );
        }
        if (signedArgs.size() != 1) {
            throw new IllegalStateException();
        }
        return ArgumentParseResult.successFuture(
            new SignedStringImpl(
                str,
                signedArgs.entrySet().iterator().next().getValue(),
                this.serverHolder
            )
        );
    }

    private record SignedStringImpl(String string, PlayerChatMessage signedMessage, MinecraftServerHolder serverHolder) implements SignedGreedyStringParser.SignedString {

        @Override
        public void sendMessage(final Audience audience, final Component unsigned) {
            final net.minecraft.network.chat.Component nativeComponent = FabricServerAudiences.of(this.serverHolder.requireServer()).toNative(unsigned);
            final PlayerChatMessage playerChatMessage = this.signedMessage.withUnsignedContent(nativeComponent);
            audience.sendMessage(playerChatMessage, ChatType.chatType(FabricChatHandler.CHAT_TYPE_KEY).bind(unsigned));
        }
    }
}
