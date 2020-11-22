package net.draycia.carbon.common.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerUserArgument<C extends CarbonUser> extends CommandArgument<C, PlayerUser> {

  private PlayerUserArgument(final @NonNull String command, final boolean required) {
    super(required, "user", PlayerUserArgument::parser,
      "",
      PlayerUser.class, (context, input) -> {
        final List<String> suggestions = CarbonChatProvider.carbonChat().userService().onlineUsers().stream()
          .map(PlayerUser::name).collect(Collectors.toCollection(ArrayList::new));

        if (context.getSender().hasPermission("carbonchat.command.completions." + command)) {
          suggestions.addAll(CarbonChatProvider.carbonChat().userService().proxyPlayers());
        }

        return suggestions;
      });
  }

  private static <C extends CarbonUser> @NonNull ArgumentParseResult<PlayerUser> parser(final @NonNull CommandContext<C> commandContext,
                                                            final @NonNull Queue<String> inputs) {
    final String input = inputs.poll();

    final String playerNotFound = CarbonChatProvider.carbonChat().translations().exceptionMessages()
      .playerNotFound();

    if (input == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(playerNotFound));
    }

    final UUID uuid = CarbonChatProvider.carbonChat().resolveUUID(input);
    final PlayerUser user = CarbonChatProvider.carbonChat().userService().wrap(uuid);

    return ArgumentParseResult.success(user);
  }

  public static <C extends CarbonUser> PlayerUserArgument<C> requiredPlayerUserArgument(final @NonNull String command) {
    return new PlayerUserArgument<>(command, true);
  }

  public static <C extends CarbonUser> PlayerUserArgument<C> optionalPlayerUserArgument(final @NonNull String command) {
    return new PlayerUserArgument<>(command, false);
  }

}
