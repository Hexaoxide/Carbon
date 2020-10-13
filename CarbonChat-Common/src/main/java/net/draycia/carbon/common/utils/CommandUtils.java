package net.draycia.carbon.common.utils;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.UserService;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Queue;

public final class CommandUtils {

  private CommandUtils() {

  }

  public static CommandArgument<CarbonUser, CarbonUser> optionalChatUserArgument() {
    return CommandArgument.<CarbonUser, CarbonUser>ofType(CarbonUser.class, "user")
      .asOptional()
      .withParser(CommandUtils::parse)
      .build();
  }

  // TODO: turn this into a proper class that handles suggestions
  public static CommandArgument<CarbonUser, CarbonUser> chatUserArgument() {
    return CommandArgument.<CarbonUser, CarbonUser>ofType(CarbonUser.class, "user")
      .asRequired()
      .withParser(CommandUtils::parse)
      .build();
  }

  private static @NonNull ArgumentParseResult<CarbonUser> parse(final CommandContext<CarbonUser> c, final Queue<String> i) {
    final String input = i.poll();

    if (input == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException("Player cannot be null"));
    }

    final UserService<?> userService = CarbonChatProvider.carbonChat().userService();
    final CarbonUser user = userService.wrap(userService.resolve(input));

    if (user != null) {
      return ArgumentParseResult.success(user);
    } else {
      return ArgumentParseResult.failure(new IllegalArgumentException("Player does not exist"));
    }
  }
}
