package net.draycia.carbon.common.commands.arguments;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.config.ExceptionMessages;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public final class ChannelArgument<C> extends CommandArgument<C, TextChannel> {

  private ChannelArgument(final boolean required) {
    super(required, "channel", ChannelArgument::parser,
      CarbonChatProvider.carbonChat().channelRegistry().defaultValue().key(),
      TextChannel.class, ChannelArgument::suggestions);
  }

  private static <C> ArgumentParseResult<TextChannel> parser(@NonNull final CommandContext<C> commandContext,
                                                             @NonNull final Queue<String> inputs) {
    final String input = inputs.poll();

    final ExceptionMessages exceptions = CarbonChatProvider.carbonChat().translations().exceptionMessages();

    if (input == null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(exceptions.channelNotFound()));
    }

    final ChatChannel channel = CarbonChatProvider.carbonChat()
      .channelRegistry().get(input);

    if (channel instanceof TextChannel) {
      return ArgumentParseResult.success((TextChannel) channel);
    } else if (channel != null) {
      return ArgumentParseResult.failure(new IllegalArgumentException(exceptions.channelNotPublic()));
    } else {
      return ArgumentParseResult.failure(new IllegalArgumentException(exceptions.channelNotFound()));
    }
  }

  private static <C> List<String> suggestions(@NonNull final CommandContext<C> commandContext,
                                              @NonNull final String input) {
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
