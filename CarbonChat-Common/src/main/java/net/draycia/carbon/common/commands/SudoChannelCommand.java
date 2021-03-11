package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SudoChannelCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public SudoChannelCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("sudochannel");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // console & player
        .permission("carbonchat.channel.others")
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name()))
        .argument(ChannelArgument.requiredChannelArgument())
        .handler(this::otherChannel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // console & player
        .permission("carbonchat.channel.others.message")
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name()))
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().asOptional().build())
        .handler(this::sendMessageOther)
        .build()
    );
  }

  private void otherChannel(final @NonNull CommandContext<CarbonUser> context) {
    final CarbonUser sender = context.getSender();
    final PlayerUser user = context.get("user");
    final ChatChannel channel = context.get("channel");

    user.selectedChannel(channel);

    final String message = channel.switchMessage();
    final String otherMessage = channel.switchOtherMessage();

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message,
      Template.of("color", "<color:" + channel.channelColor(user).toString() + ">"),
      Template.of("channel", channel.name())));

    sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(otherMessage,
      Template.of("color", "<color:" + channel.channelColor(user).toString() + ">"),
      Template.of("channel", channel.name()), Template.of("player", user.name())));
  }

  private void sendMessageOther(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = context.get("user");
    final ChatChannel channel = context.get("channel");
    final String message = context.get("message");

    channel.sendComponentsAndLog(user.identity(), channel.parseMessage(user, message, false));
  }

}
