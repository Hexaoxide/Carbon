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
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.exception.ComponentException;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.users.ProfileResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;

@DefaultQualifier(NonNull.class)
public final class CarbonPlayerParser implements ArgumentParser.FutureArgumentParser<Commander, CarbonPlayer>, ParserDescriptor<Commander, CarbonPlayer> {

    private final PlayerSuggestions suggestions;
    private final UserManager<?> userManager;
    private final ProfileResolver profileResolver;
    private final CarbonMessages messages;

    @Inject
    private CarbonPlayerParser(
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
    public CompletableFuture<ArgumentParseResult<CarbonPlayer>> parseFuture(
        final CommandContext<Commander> commandContext,
        final CommandInput commandInput
    ) {
        final String input = commandInput.readString();
        return this.profileResolver.resolveUUID(input, commandContext.isSuggestions()).thenCompose(uuid -> {
            if (uuid == null) {
                return ArgumentParseResult.failureFuture(new ParseException(input, this.messages));
            }
            return this.userManager.user(uuid).thenApply(ArgumentParseResult::success);
        });
    }

    @Override
    public @NonNull SuggestionProvider<Commander> suggestionProvider() {
        return this.suggestions;
    }

    @Override
    public @NonNull TypeToken<CarbonPlayer> valueType() {
        return TypeToken.get(CarbonPlayer.class);
    }

    @Override
    public @NonNull ArgumentParser<Commander, CarbonPlayer> parser() {
        return this;
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
