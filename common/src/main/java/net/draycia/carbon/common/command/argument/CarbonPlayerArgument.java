//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package net.draycia.carbon.common.command.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Argument that parses into a {@link CarbonPlayer}.
 */
@DefaultQualifier(NonNull.class)
public final class CarbonPlayerArgument extends CommandArgument<Commander, CarbonPlayer> {

    private CarbonPlayerArgument(
        final boolean required,
        final String name,
        final String defaultValue,
        final @Nullable BiFunction<CommandContext<Commander>, String,
            List<String>> suggestionsProvider,
        final ArgumentDescription defaultDescription,
        final CarbonPlayerParser parser
    ) {
        super(required, name, parser, defaultValue, CarbonPlayer.class, suggestionsProvider, defaultDescription);
    }

    public static final class Builder extends CommandArgument.TypedBuilder<Commander, CarbonPlayer, Builder> {

        private final CarbonPlayerParser parser;

        @AssistedInject
        private Builder(final @Assisted String name, final CarbonPlayerParser parser) {
            super(CarbonPlayer.class, name);
            this.parser = parser;
        }

        /**
         * Builder a new boolean component.
         *
         * @return constructed component
         */
        @Override
        public CarbonPlayerArgument build() {
            return new CarbonPlayerArgument(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription(),
                this.parser
            );
        }

    }

    public static final class CarbonPlayerParser implements ArgumentParser<Commander, CarbonPlayer> {

        private final PlayerSuggestions suggestions;
        private final UserManager<?> userManager;
        private final ProfileResolver profileResolver;

        @Inject
        private CarbonPlayerParser(
            final PlayerSuggestions suggestions,
            final UserManager<?> userManager,
            final ProfileResolver profileResolver
        ) {
            this.suggestions = suggestions;
            this.userManager = userManager;
            this.profileResolver = profileResolver;
        }

        @Override
        public ArgumentParseResult<CarbonPlayer> parse(
            final CommandContext<Commander> commandContext,
            final Queue<String> inputQueue
        ) {
            final @Nullable String input = inputQueue.peek();

            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                    CarbonPlayerParser.class,
                    commandContext
                ));
            }

            final @Nullable CarbonPlayer join = this.profileResolver.resolveUUID(input, commandContext.isSuggestions()).thenCompose(uuid -> {
                if (uuid == null) {
                    return CompletableFuture.completedFuture(null);
                }
                return this.userManager.user(uuid);
            }).join();

            if (join == null) {
                return ArgumentParseResult.failure(new CarbonPlayerParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(join);
        }

        @Override
        public List<String> suggestions(
            final CommandContext<Commander> commandContext,
            final String input
        ) {
            return this.suggestions.apply(commandContext, input);
        }

    }

    /**
     * CarbonPlayer parse exception.
     */
    public static final class CarbonPlayerParseException extends ParserException {

        private static final long serialVersionUID = -8331761537951077684L;
        private final String input;

        /**
         * Construct a new CarbonPlayer parse exception.
         *
         * @param input   string input
         * @param context command context
         */
        public CarbonPlayerParseException(
            final String input,
            final CommandContext<?> context
        ) {
            super(
                CarbonPlayerParser.class,
                context,
                Caption.of("argument.parse.failure.player"),
                CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input.
         *
         * @return string value
         */
        public String input() {
            return this.input;
        }

    }

}
