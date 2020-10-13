package net.draycia.carbon.common.commands.arguments;

import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParseResult;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public final class ChannelArgument<C> extends CommandArgument<C, TextChannel> {

  private ChannelArgument(final boolean required) {
    super(required, "channel", ChannelArgument::parser,
      CarbonChatProvider.carbonChat().channelRegistry().defaultChannel().key(),
      TextChannel.class, ChannelArgument::suggestions);
  }

  private static <C> ArgumentParseResult<TextChannel> parser(final @NonNull CommandContext<C> commandContext,
                                                             final @NonNull Queue<String> inputs) {
    final String input = inputs.poll();

    if (input == null) {
      // TODO: make exception messages configurable
      return ArgumentParseResult.failure(new IllegalArgumentException("Channel cannot be null"));
    }

    final ChatChannel channel = CarbonChatProvider.carbonChat()
      .channelRegistry().get(input);

    if (channel instanceof TextChannel) {
      return ArgumentParseResult.success((TextChannel) channel);
    } else if (channel != null) {
      return ArgumentParseResult.failure(new IllegalArgumentException("Channel is not public!"));
    } else {
      return ArgumentParseResult.failure(new IllegalArgumentException("Channel does not exist"));
    }
  }

  private static <C> List<String> suggestions(final @NonNull CommandContext<C> commandContext,
                                              final @NonNull String input) {
    return CarbonChatProvider.carbonChat().channelRegistry().stream()
      .map(ChatChannel::key).collect(Collectors.toList());
  }

  public static <C> ChannelArgument<C> requiredChannelArgument() {
    return new ChannelArgument<>(true);
  }

  public static <C> ChannelArgument<C> optionalChannelArgument() {
    return new ChannelArgument<>(false);
  }

}
