package net.draycia.carbon.common.utils;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;

public final class CommandUtils {

  private CommandUtils() {

  }

  // TODO: turn this into a proper class that handles suggestions
  private static final CommandComponent<ChatUser, ChatChannel> commandComponent =
    CommandComponent.<ChatUser, ChatChannel>ofType(ChatChannel.class, "channel")
      .asRequired()
      .withParser((c, i) -> {
        final String input = i.peek();

        if (input == null) {
          return ComponentParseResult.failure(new IllegalArgumentException("Channel cannot be null"));
        }

        final ChatChannel channel = CarbonChatProvider.carbonChat()
          .channelRegistry().get(input);

        if (channel != null) {
          return ComponentParseResult.success(channel);
        } else {
          return ComponentParseResult.failure(new IllegalArgumentException("Channel does not exist"));
        }
      })
      .build();

  public static CommandComponent<ChatUser, ChatChannel> channelComponent() {
    return commandComponent;
  }

  // TODO: turn this into a proper class that handles suggestions
  private static final CommandComponent<ChatUser, ChatUser> chatUserComponent =
    CommandComponent.<ChatUser, ChatUser>ofType(ChatUser.class, "user")
      .asRequired()
      .withParser((c, i) -> {
        final String input = i.peek();

        if (input == null) {
          return ComponentParseResult.failure(new IllegalArgumentException("Player cannot be null"));
        }

        final ChatUser user = CarbonChatProvider.carbonChat()
          .userService().wrap(null); // TODO: find way to resolve name -> uuid

        if (user != null) {
          return ComponentParseResult.success(user);
        } else {
          return ComponentParseResult.failure(new IllegalArgumentException("Player does not exist"));
        }
      })
      .build();

  public static CommandComponent<ChatUser, ChatUser> chatUserComponent() {
    return chatUserComponent;
  }

}
