package net.draycia.carbon.common.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.PlayerUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class PlayerUserArgument<C> extends CommandArgument<C, PlayerUser> {

  private PlayerUserArgument(final boolean required) {
    super(required, "user", PlayerUserArgument::parser,
      "",
      PlayerUser.class, PlayerUserArgument::suggestions);
  }

  private static <C> ArgumentParseResult<PlayerUser> parser(@NonNull final CommandContext<C> commandContext,
                                                            @NonNull final Queue<String> inputs) {
    final String input = inputs.poll();

    final String playerNotFound = CarbonChatProvider.carbonChat().translations().exceptionMessages()
      .playerNotFound();

    if (input == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(playerNotFound));
    }

    final UUID uuid = CarbonChatProvider.carbonChat().userService().resolve(input);

    if (uuid == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(playerNotFound));
    }

    final PlayerUser user = CarbonChatProvider.carbonChat().userService().wrap(uuid);

    if (user == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(playerNotFound));
    }

    return ArgumentParseResult.success(user);
  }

  private static <C> List<String> suggestions(@NonNull final CommandContext<C> commandContext,
                                              @NonNull final String input) {
    return StreamSupport
      .stream(CarbonChatProvider.carbonChat().userService().onlineUsers().spliterator(), false)
      .map(PlayerUser::name)
      .collect(Collectors.toList());
  }

  public static <C> PlayerUserArgument<C> requiredPlayerUserArgument() {
    return new PlayerUserArgument<>(true);
  }

  public static <C> PlayerUserArgument<C> optionalPlayerUserArgument() {
    return new PlayerUserArgument<>(false);
  }

}
