package net.draycia.carbon.common.utils;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;

public final class CommandUtils {

  private CommandUtils() {

  }

  // TODO: turn this into a proper class that handles suggestions
  public static CommandArgument<ChatUser, ChatChannel> channelArgument() {
    return CommandArgument.<ChatUser, ChatChannel>ofType(ChatChannel.class, "channel")
      .asRequired()
      .withParser((c, i) -> {
        final String input = i.peek();

        if (input == null) {
          return ArgumentParseResult.failure(new IllegalArgumentException("Channel cannot be null"));
        }

        final ChatChannel channel = CarbonChatProvider.carbonChat()
          .channelRegistry().get(input);

        if (channel != null) {
          return ArgumentParseResult.success(channel);
        } else {
          return ArgumentParseResult.failure(new IllegalArgumentException("Channel does not exist"));
        }
      })
      .build();
  }

  // TODO: turn this into a proper class that handles suggestions
  public static CommandArgument<ChatUser, ChatUser> chatUserArgument() {
    return CommandArgument.<ChatUser, ChatUser>ofType(ChatUser.class, "user")
      .asRequired()
      .withParser((c, i) -> {
        final String input = i.peek();

        if (input == null) {
          return ArgumentParseResult.failure(new IllegalArgumentException("Player cannot be null"));
        }

        final ChatUser user = CarbonChatProvider.carbonChat()
          .userService().wrap(null); // TODO: find way to resolve name -> uuid

        if (user != null) {
          return ArgumentParseResult.success(user);
        } else {
          return ArgumentParseResult.failure(new IllegalArgumentException("Player does not exist"));
        }
      })
      .build();
  }

}
