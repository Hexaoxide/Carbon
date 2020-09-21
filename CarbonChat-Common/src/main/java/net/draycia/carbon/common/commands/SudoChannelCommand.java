package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.common.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SudoChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SudoChannelCommand(@NonNull final CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("sudochannel");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel.others")
        .argument(CommandUtils.chatUserArgument())
        .argument(CommandUtils.channelArgument())
        .handler(this::otherChannel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel.others.message")
        .argument(CommandUtils.chatUserArgument())
        .argument(CommandUtils.channelArgument())
        .argument(StringArgument.<ChatUser>newBuilder("message").greedy().build())
        .handler(this::sendMessageOther)
        .build()
    );
  }

  private void otherChannel(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser user = context.getRequired("user");
    final ChatChannel channel = context.getRequired("channel");

    user.selectedChannel(channel);

    final String message = channel.switchMessage();
    final String otherMessage = channel.switchOtherMessage();

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));

    sender.sendMessage(
      this.carbonChat.messageProcessor().processMessage(otherMessage, "br", "\n",
        "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name(),
        "player", user.name()));
  }

  private void sendMessageOther(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser user = context.getRequired("user");
    final ChatChannel channel = context.getRequired("channel");
    final String message = context.getRequired("message");

    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.messageProcessor().audiences().console().sendMessage(component);
  }

}
