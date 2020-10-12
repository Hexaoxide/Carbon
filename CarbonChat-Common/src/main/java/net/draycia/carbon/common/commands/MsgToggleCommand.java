package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MsgToggleCommand {

  private @NonNull final CarbonChat carbonChat;

  public MsgToggleCommand(@NonNull final CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("msgtoggle");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player & console
        .withPermission("carbonchat.msgtoggle")
        .argument(CommandUtils.optionalChatUserArgument()) // carbonchat.msgtoggle.other
        .handler(context -> {
          if (context.get("user").isPresent()) {
            this.toggleOther(context);
          } else {
            this.toggleSelf(context);
          }
        })
        .build()
    );
  }

  private void toggleSelf(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatChannel channel = this.carbonChat.channelRegistry().get("whisper");

    final String message;

    final UserChannelSettings settings = user.channelSettings(channel);

    if (!channel.ignorable()) {
      message = channel.cannotIgnoreMessage();
    } else if (settings.ignored()) {
      settings.ignoring(false);
      message = channel.toggleOffMessage();
    } else {
      settings.ignoring(true);
      message = channel.toggleOnMessage();
    }

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message));
  }

  private void toggleOther(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser user = context.getRequired("user");
    final ChatChannel channel = this.carbonChat.channelRegistry().get("whisper");

    final String message;
    final String otherMessage;

    final UserChannelSettings settings = user.channelSettings(channel);

    if (settings.ignored()) {
      settings.ignoring(false);
      message = channel.toggleOffMessage();
      otherMessage = channel.toggleOtherOffMessage();
    } else {
      settings.ignoring(true);
      message = channel.toggleOnMessage();
      otherMessage = channel.toggleOtherOnMessage();
    }

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message));

    sender.sendMessage(this.carbonChat.messageProcessor().processMessage(otherMessage,
      "player", user.name()));
  }
}
