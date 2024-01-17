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
package net.draycia.carbon.common.command.argument;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.StringParser;

@DefaultQualifier(NonNull.class)
public final class SignedGreedyStringParser implements
    ArgumentParser.FutureArgumentParser<Commander, SignedGreedyStringParser.SignedString>,
    ParserDescriptor<Commander, SignedGreedyStringParser.SignedString> {

    private final ArgumentParser<Commander, SignedString> wrapped;

    @Inject
    public SignedGreedyStringParser(final Mapper mapper) {
        this.wrapped = StringParser.<Commander>greedyStringParser().parser().flatMapSuccess(mapper);
    }

    @Override
    public CompletableFuture<ArgumentParseResult<SignedString>> parseFuture(
        final CommandContext<Commander> commandContext,
        final CommandInput commandInput
    ) {
        return this.wrapped.parseFuture(commandContext, commandInput);
    }

    @Override
    public ArgumentParser<Commander, SignedString> parser() {
        return this;
    }

    @Override
    public TypeToken<SignedString> valueType() {
        return TypeToken.get(SignedString.class);
    }

    @FunctionalInterface
    public interface Mapper extends BiFunction<CommandContext<Commander>,
        String, CompletableFuture<ArgumentParseResult<SignedString>>> {
    }

    public static final class NonSignedMapper implements Mapper {

        @Override
        public CompletableFuture<ArgumentParseResult<SignedString>> apply(
            final CommandContext<Commander> commanderCommandContext,
            final String s
        ) {
            return ArgumentParseResult.successFuture(new NonSignedString(s));
        }
    }

    public interface SignedString {
        String string();

        @Nullable SignedMessage signedMessage();

        void sendMessage(Audience audience, Component unsigned);
    }

    public record NonSignedString(String string) implements SignedString {

        @Override
        public @Nullable SignedMessage signedMessage() {
            return null;
        }

        @Override
        public void sendMessage(final Audience audience, final Component unsigned) {
            audience.sendMessage(unsigned);
        }
    }
}
