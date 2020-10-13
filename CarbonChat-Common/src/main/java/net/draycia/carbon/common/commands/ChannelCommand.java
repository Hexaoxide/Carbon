package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelCommand {

  private @NonNull final CarbonChat carbonChat;

  public ChannelCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("channel");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(PlayerUser.class) // player
        .withPermission("carbonchat.channel")
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.optional("message")) // carbonchat.channel.message
        .handler(context -> {
          if (context.get("message").isPresent()) {
            this.sendMessage(context);
          } else {
            this.channel(context);
          }
        })
        .build()
    );
  }

  private void channel(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final TextChannel channel = context.getRequired("channel");

    if (user.channelSettings(channel).ignored()) {
      user.sendMessage(this.carbonChat.messageProcessor().processMessage(channel.cannotUseMessage(),
        "color", "<" + channel.channelColor(user).toString() + ">",
        "channel", channel.name()));

      return;
    }

    user.selectedChannel(channel);
  }

  private void sendMessage(@NonNull final CommandContext<CarbonUser> context) {
    final ChatChannel channel = context.getRequired("channel");
    final String message = context.getRequired("message");

    channel.sendComponentsAndLog(channel.parseMessage((PlayerUser) context.getSender(),
      message, false));
  }

}
