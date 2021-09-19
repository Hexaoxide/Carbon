package net.draycia.carbon.common.command.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.List;
import java.util.Queue;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CarbonPlayerArgument {

    final Injector injector;

    @Inject
    public CarbonPlayerArgument(final Injector injector) {
        this.injector = injector;
    }

    public CommandArgument<Commander, CarbonPlayer> newInstance(boolean required, final String name) {
        return new CarbonPlayerArgumentImpl(required, name, this.injector.getInstance(CarbonPlayerArgumentParser.class), CarbonPlayer.class);
    }

    public static final class CarbonPlayerArgumentImpl extends CommandArgument<Commander, CarbonPlayer> {

        CarbonPlayerArgumentImpl(boolean required, @NonNull String name, @NonNull ArgumentParser<Commander, CarbonPlayer> parser, @NonNull Class<CarbonPlayer> valueType) {
            super(required, name, parser, valueType);
        }

    }

    private static final class CarbonPlayerArgumentParser implements ArgumentParser<Commander, CarbonPlayer> {

        final CarbonChat carbonChat;

        @Inject
        public CarbonPlayerArgumentParser(final CarbonChat carbonChat) {
            this.carbonChat = carbonChat;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull CarbonPlayer> parse(@NonNull CommandContext<@NonNull Commander> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.poll();

            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(CarbonPlayerArgumentParser.class, commandContext));
            }

            for (var player : this.carbonChat.server().players()) {
                if (player.username().equalsIgnoreCase(input)) {
                    return ArgumentParseResult.success(player);
                }
            }

            return ArgumentParseResult.failure(new PlayerParseException(input, commandContext));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<Commander> commandContext, @NonNull String input) {
            return ArgumentParser.super.suggestions(commandContext, input);
        }

    }

    public static final class PlayerParseException extends ParserException {

        private static final long serialVersionUID = -8703083769083215214L;
        private final String input;

        public PlayerParseException(
            final @NonNull String input,
            final @NonNull CommandContext<?> context
        ) {
            super(
                CarbonPlayerArgumentParser.class,
                context,
                Caption.of("No player with that name was found!"),
                CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public @NonNull String input() {
            return this.input;
        }

    }

}
