package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SudoChannelCommand {

  private @NonNull final CarbonChat carbonChat;

  public SudoChannelCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("sudochannel");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.channel.others.message")
        .argument(PlayerUserArgument.requiredPlayerUserArgument())
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().asOptional().build())
        .handler(context -> {
          if (context.getOptional("message").isPresent()) {
            this.sendMessageOther(context);
          } else {
            this.otherChannel(context);
          }
        })
        .build()
    );
  }

  private void otherChannel(@NonNull final CommandContext<CarbonUser> context) {
    final CarbonUser sender = context.getSender();
    final CarbonUser user = context.get("user");
    final ChatChannel channel = context.get("channel");

    user.selectedChannel(channel);

    final String message = channel.switchMessage();
    final String otherMessage = channel.switchOtherMessage();

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message,
      "color", "<color:" + channel.channelColor(user).toString() + ">",
      "channel", channel.name()));

    sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(otherMessage,
        "color", "<color:" + channel.channelColor(user).toString() + ">",
        "channel", channel.name(), "player", user.name()));
  }

  private void sendMessageOther(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = context.get("user");
    final ChatChannel channel = context.get("channel");
    final String message = context.get("message");

    channel.sendComponentsAndLog(channel.parseMessage(user, message, false));
  }

}
