package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
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
        .withSenderType(CarbonUser.class) // player & console
        .withPermission("carbonchat.channel.others.message")
        .argument(PlayerUserArgument.requiredPlayerUserArgument())
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().asOptional().build())
        .handler(context -> {
          if (context.get("message").isPresent()) {
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
    final CarbonUser user = context.getRequired("user");
    final ChatChannel channel = context.getRequired("channel");

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
    final PlayerUser user = context.getRequired("user");
    final ChatChannel channel = context.getRequired("channel");
    final String message = context.getRequired("message");

    channel.sendComponentsAndLog(channel.parseMessage(user, message, false));
  }

}
