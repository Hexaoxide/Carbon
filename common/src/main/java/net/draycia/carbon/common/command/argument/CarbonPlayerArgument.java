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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessageService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument that parses into a {@link CarbonPlayer}.
 */
@SuppressWarnings("unused")
public final class CarbonPlayerArgument extends CommandArgument<Commander, CarbonPlayer> {

    private CarbonPlayerArgument(
        final boolean required,
        final @NonNull String name,
        final @NonNull String defaultValue,
        final @Nullable BiFunction<@NonNull CommandContext<Commander>, @NonNull String,
            @NonNull List<@NonNull String>> suggestionsProvider,
        final @NonNull ArgumentDescription defaultDescription,
        final @NonNull CarbonMessageService messageService
    ) {
        super(required, name, new CarbonPlayerParser(messageService), defaultValue, CarbonPlayer.class, suggestionsProvider, defaultDescription);
    }

    /**
     * Create a new builder.
     *
     * @param name name of the component
     * @return created builder
     */
    public static @NonNull Builder newBuilder(final @NonNull String name) {
        return new Builder(name);
    }

    /**
     * Create a new required command component.
     *
     * @param name component name
     * @return created component
     */
    public static @NonNull CommandArgument<Commander, CarbonPlayer> of(final @NonNull String name) {
        return CarbonPlayerArgument.newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component.
     *
     * @param name component name
     * @return created component
     */
    public static @NonNull CommandArgument<Commander, CarbonPlayer> optional(final @NonNull String name) {
        return CarbonPlayerArgument.newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value.
     *
     * @param name component name
     * @param defaultCarbonPlayer default player
     * @return created component
     */
    public static @NonNull CommandArgument<Commander, CarbonPlayer> optional(
        final @NonNull String name,
        final @NonNull String defaultCarbonPlayer
    ) {
        return CarbonPlayerArgument.newBuilder(name).asOptionalWithDefault(defaultCarbonPlayer).build();
    }

    public static final class Builder extends CommandArgument.Builder<Commander, CarbonPlayer> {

        private CarbonMessageService messageService;

        private Builder(final @NonNull String name) {
            super(CarbonPlayer.class, name);
        }

        /**
         * Set the message service.
         *
         * @param messageService the message service
         * @return builder instance
         */
        public Builder withMessageService(final @NonNull CarbonMessageService messageService) {
            this.messageService = messageService;
            return this;
        }

        /**
         * Builder a new boolean component.
         *
         * @return constructed component
         */
        @Override
        public @NonNull CarbonPlayerArgument build() {
            return new CarbonPlayerArgument(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription(),
                this.messageService
            );
        }

    }

    public static final class CarbonPlayerParser implements ArgumentParser<Commander, CarbonPlayer> {

        private final @NonNull CarbonMessageService messageService;

        public CarbonPlayerParser(final @NonNull CarbonMessageService messageService) {
            this.messageService = messageService;
        }

        @Override
        @SuppressWarnings("deprecation")
        public @NonNull ArgumentParseResult<CarbonPlayer> parse(
            final @NonNull CommandContext<Commander> commandContext,
            final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();

            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                    CarbonPlayerParser.class,
                    commandContext
                ));
            }

            return CarbonChatProvider.carbonChat().server().resolveUUID(input).thenApply(uuid -> {
                if (uuid == null) {
                    return ArgumentParseResult.<CarbonPlayer>failure(new CarbonPlayerParseException(input, commandContext, this.messageService));
                }

                final var playerResult = CarbonChatProvider.carbonChat().server().userManager().carbonPlayer(uuid).join();

                if (playerResult.player() == null) {
                    return ArgumentParseResult.<CarbonPlayer>failure(new CarbonPlayerParseException(input, commandContext, this.messageService));
                }

                inputQueue.remove();

                return ArgumentParseResult.<CarbonPlayer>success(playerResult.player());
            }).join();
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
            final @NonNull CommandContext<Commander> commandContext,
            final @NonNull String input
        ) {
            throw new UnsupportedOperationException("Suggestions provider must be overridden.");
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
         * @param input string input
         * @param context command context
         */
        public CarbonPlayerParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context,
            final @NonNull CarbonMessageService messageService
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
        public @NonNull String input() {
            return this.input;
        }

    }

}
