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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.exception.ComponentException;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerArgument extends CommandArgument<Commander, CarbonPlayer> {

    private CarbonPlayerArgument(
        final boolean required,
        final String name,
        final String defaultValue,
        final @Nullable BiFunction<CommandContext<Commander>, String,
            List<String>> suggestionsProvider,
        final ArgumentDescription defaultDescription,
        final Parser parser
    ) {
        super(required, name, parser, defaultValue, CarbonPlayer.class, suggestionsProvider, defaultDescription);
    }

    public static final class Builder extends CommandArgument.TypedBuilder<Commander, CarbonPlayer, Builder> {

        private final Parser parser;

        @AssistedInject
        private Builder(final @Assisted String name, final Parser parser) {
            super(CarbonPlayer.class, name);
            this.parser = parser;
        }

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

    public static final class Parser implements ArgumentParser<Commander, CarbonPlayer> {

        // This hack only works properly when there is 0 or 1 CarbonPlayerArguments in a chain, since we don't use the arg name
        public static CloudKey<String> INPUT_STRING = SimpleCloudKey.of(Parser.class.getSimpleName() + "-input", TypeToken.get(String.class));

        private final PlayerSuggestions suggestions;
        private final UserManager<?> userManager;
        private final ProfileResolver profileResolver;
        private final CarbonMessages messages;

        @Inject
        private Parser(
            final PlayerSuggestions suggestions,
            final UserManager<?> userManager,
            final ProfileResolver profileResolver,
            final CarbonMessages messages
        ) {
            this.suggestions = suggestions;
            this.userManager = userManager;
            this.profileResolver = profileResolver;
            this.messages = messages;
        }

        @Override
        public ArgumentParseResult<CarbonPlayer> parse(
            final CommandContext<Commander> commandContext,
            final Queue<String> inputQueue
        ) {
            final String input = inputQueue.peek();

            final @Nullable CarbonPlayer join = this.profileResolver.resolveUUID(input, commandContext.isSuggestions()).thenCompose(uuid -> {
                if (uuid == null) {
                    return CompletableFuture.completedFuture(null);
                }
                return this.userManager.user(uuid);
            }).join();

            if (join == null) {
                return ArgumentParseResult.failure(new ParseException(input, this.messages));
            }

            commandContext.store(INPUT_STRING, input);
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

    public static final class ParseException extends ComponentException {

        private static final long serialVersionUID = -8331761537951077684L;
        private final String input;

        public ParseException(final String input, final CarbonMessages messages) {
            super(messages.errorCommandInvalidPlayer(input));
            this.input = input;
        }

        public String input() {
            return this.input;
        }

    }

}
