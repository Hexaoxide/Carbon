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

public class ToggleCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ToggleCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("toggle");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player & console
        .withPermission("carbonchat.toggle")
        .argument(CommandUtils.channelArgument())
        .argument(CommandUtils.optionalChatUserArgument()) // carbonchat.toggle.other
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

  private void toggleSelf(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatChannel channel = context.getRequired("channel");

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

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));
  }

  private void toggleOther(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser user = context.getRequired("user");
    final ChatChannel channel = context.getRequired("channel");

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

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));

    sender.sendMessage(
      this.carbonChat.messageProcessor().processMessage(otherMessage,
        "br", "\n", "color", "<color:" + channel.channelColor(user).toString() + ">",
        "channel", channel.name(), "player", user.name()));
  }
}
