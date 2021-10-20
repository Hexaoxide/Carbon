package net.draycia.carbon.common.command.argument;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonPlayerArgument {

    private final CarbonServer server;

    @Inject
    public CarbonPlayerArgument(final CarbonServer server) {
        this.server = server;
    }

    public static BiPredicate<CarbonPlayer, CarbonPlayer> NO_FILTER = (sender, target) -> true;
    public static BiPredicate<CarbonPlayer, CarbonPlayer> NO_SENDER = (sender, target) -> !sender.equals(target);

    public CommandArgument<Commander, CarbonPlayer> newInstance(
        final boolean required,
        final String name,
        final BiPredicate<CarbonPlayer, CarbonPlayer> filter
    ) {
        return new CarbonPlayerArgumentImpl(required, name,
            new CarbonPlayerArgumentParser(this.server, filter), CarbonPlayer.class);
    }

    public CommandArgument<Commander, CarbonPlayer> newInstance(
        final boolean required,
        final String name
    ) {
        return new CarbonPlayerArgumentImpl(required, name,
            new CarbonPlayerArgumentParser(this.server, NO_FILTER), CarbonPlayer.class);
    }

    public static final class CarbonPlayerArgumentImpl extends CommandArgument<Commander, CarbonPlayer> {

        CarbonPlayerArgumentImpl(
            final boolean required,
            final String name,
            final ArgumentParser<Commander, CarbonPlayer> parser,
            final Class<CarbonPlayer> valueType
        ) {
            super(required, name, parser, valueType);
        }

    }

    private static final class CarbonPlayerArgumentParser implements ArgumentParser<Commander, CarbonPlayer> {

        final CarbonServer server;
        final BiPredicate<CarbonPlayer, CarbonPlayer> filter;

        @Inject
        public CarbonPlayerArgumentParser(
            final CarbonServer carbonServer,
            final BiPredicate<CarbonPlayer, CarbonPlayer> filter
        ) {
            this.server = carbonServer;
            this.filter = filter;
        }

        @Override
        public ArgumentParseResult<CarbonPlayer> parse(
            final CommandContext<Commander> commandContext,
            final Queue<String> inputQueue
        ) {
            final @Nullable String input = inputQueue.peek();

            for (var player : this.server.players()) {
                if (player.username().equalsIgnoreCase(input)) {
                    inputQueue.remove();
                    return ArgumentParseResult.success(player);
                }
            }

            return ArgumentParseResult.failure(new PlayerParseException(input));
        }

        @Override
        public List<String> suggestions(
            final CommandContext<Commander> commandContext,
            final String input
        ) {
            if (commandContext.getSender() instanceof PlayerCommander sender) {
                return this.server.players().stream()
                    .filter(it -> filter.test(sender.carbonPlayer(), it))
                    .filter(sender.carbonPlayer()::awareOf)
                    .map(CarbonPlayer::username)
                    .toList();
            } else {
                return this.server.players().stream()
                    .map(CarbonPlayer::username)
                    .toList();
            }
        }

    }

    public static final class PlayerParseException extends RuntimeException {

        private static final long serialVersionUID = -8703083769083215214L;
        private final String input;

        public PlayerParseException(
            final String input
        ) {
            this.input = input;
        }

        public String input() {
            return this.input;
        }

        @Override
        public String getMessage() {
            return "No player found for input: '" + this.input + "'";
        }
    }

}
