package net.draycia.carbon.common.command.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import java.util.List;
import java.util.Queue;
import java.util.stream.StreamSupport;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CarbonPlayerArgument {

    private final CarbonServer server;

    @Inject
    public CarbonPlayerArgument(final CarbonServer server) {
        this.server = server;
    }

    public CommandArgument<Commander, CarbonPlayer> newInstance(boolean required, final String name) {
        return new CarbonPlayerArgumentImpl(required, name, new CarbonPlayerArgumentParser(this.server), CarbonPlayer.class);
    }

    public static final class CarbonPlayerArgumentImpl extends CommandArgument<Commander, CarbonPlayer> {

        CarbonPlayerArgumentImpl(boolean required, @NonNull String name, @NonNull ArgumentParser<Commander, CarbonPlayer> parser, @NonNull Class<CarbonPlayer> valueType) {
            super(required, name, parser, valueType);
        }

    }

    private static final class CarbonPlayerArgumentParser implements ArgumentParser<Commander, CarbonPlayer> {

        final CarbonServer server;

        @Inject
        public CarbonPlayerArgumentParser(final CarbonServer carbonServer) {
            this.server = carbonServer;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull CarbonPlayer> parse(@NonNull CommandContext<@NonNull Commander> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
            final String input = inputQueue.peek();

            for (var player : this.server.players()) {
                if (player.username().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ArgumentParseResult.success(player);
                }
            }

            return ArgumentParseResult.failure(new PlayerParseException(input));
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<Commander> commandContext, @NonNull String input) {
            return StreamSupport.stream(this.server.players().spliterator(), false)
                .map(CarbonPlayer::username)
                .toList();
        }

    }

    public static final class PlayerParseException extends RuntimeException {

        private static final long serialVersionUID = -8703083769083215214L;
        private final String input;

        public PlayerParseException(
            final @NonNull String input
        ) {
            this.input = input;
        }

        public @NonNull String input() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return "No player found for input: '" + this.input + "'";
        }
    }

}
