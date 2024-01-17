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
package net.draycia.carbon.paper.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.argument.SignedGreedyStringParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ArgumentParseResult;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory;
import xyz.jpenilla.reflectionremapper.proxy.annotation.MethodName;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type;

@DefaultQualifier(NonNull.class)
@Singleton
public final class PaperSignedMapper implements SignedGreedyStringParser.Mapper {

    private static final Key PAPER_RAW_CHAT_TYPE = Key.key("paper", "raw");

    private final CommandSourceStackProxy commandSourceStackProxy;
    private final SignedArgumentsProxy signedArgumentsProxy;
    private final PlayerChatMessageProxy playerChatMessageProxy;
    private final PaperAdventureProxy paperAdventureProxy;

    @Inject
    private PaperSignedMapper() {
        final ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
        final ReflectionProxyFactory proxyFactory = ReflectionProxyFactory.create(remapper, this.getClass().getClassLoader());
        this.commandSourceStackProxy = proxyFactory.reflectionProxy(CommandSourceStackProxy.class);
        this.signedArgumentsProxy = proxyFactory.reflectionProxy(SignedArgumentsProxy.class);
        this.playerChatMessageProxy = proxyFactory.reflectionProxy(PlayerChatMessageProxy.class);
        this.paperAdventureProxy = proxyFactory.reflectionProxy(PaperAdventureProxy.class);
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedGreedyStringParser.SignedString>> apply(
        final CommandContext<Commander> ctx,
        final String str
    ) {
        final Object stack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
        final Object signingContext = this.commandSourceStackProxy.signingContext(stack);
        final Map<String, ?> signedArgs;
        try {
            signedArgs = this.signedArgumentsProxy.arguments(signingContext);
        } catch (final Throwable thr) {
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
                this
            )
        );
    }

    private record SignedStringImpl(
        String string,
        Object playerChatMessage,
        PaperSignedMapper mapper
    ) implements SignedGreedyStringParser.SignedString {

        @Override
        public SignedMessage signedMessage() {
            return this.mapper.playerChatMessageProxy.adventureView(this.playerChatMessage);
        }

        @Override
        public void sendMessage(final Audience audience, final Component unsigned) {
            final Object nativeComponent = this.mapper.paperAdventureProxy.asVanilla(unsigned);
            final Object modifiedPlayerChat = this.mapper.playerChatMessageProxy.withUnsignedContent(this.playerChatMessage, nativeComponent);
            final SignedMessage adventureView = this.mapper.playerChatMessageProxy.adventureView(modifiedPlayerChat);
            audience.sendMessage(adventureView, ChatType.chatType(PAPER_RAW_CHAT_TYPE).bind(unsigned));
        }
    }

    @Proxies(className = "net.minecraft.commands.CommandSourceStack")
    interface CommandSourceStackProxy {

        @MethodName("getSigningContext")
        Object signingContext(Object instance);
    }

    @Proxies(className = "net.minecraft.commands.CommandSigningContext$SignedArguments")
    interface SignedArgumentsProxy {

        @MethodName("arguments")
        Map<String, ?> arguments(Object instance);
    }

    @Proxies(className = "net.minecraft.network.chat.PlayerChatMessage")
    interface PlayerChatMessageProxy {

        @MethodName("adventureView")
        SignedMessage adventureView(Object instance);

        @MethodName("withUnsignedContent")
        Object withUnsignedContent(Object instance, @Type(className = "net.minecraft.network.chat.Component") Object unsignedContent);
    }

    @Proxies(className = "io.papermc.paper.adventure.PaperAdventure")
    interface PaperAdventureProxy {

        @MethodName("asVanilla")
        @Static
        Object asVanilla(Component component);
    }
}
