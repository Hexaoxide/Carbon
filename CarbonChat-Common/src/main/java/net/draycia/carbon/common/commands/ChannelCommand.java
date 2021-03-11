package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public ChannelCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("channel");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.channel")
        .argument(ChannelArgument.requiredChannelArgument())
        .handler(this::channel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.channel.message")
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.of("message")) // carbonchat.channel.message
        .handler(this::sendMessage)
        .build()
    );
  }

  private void channel(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final TextChannel channel = context.get("channel");

    if (!this.canUse(user, channel)) return;

    user.selectedChannel(channel);
  }

  private void sendMessage(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final ChatChannel channel = context.get("channel");
    final String message = context.get("message");

    if (!this.canUse(user, channel)) return;

    channel.sendComponentsAndLog(context.getSender().identity(), channel.parseMessage(user,
      message, false));
  }

  private boolean canUse(final PlayerUser user, final ChatChannel channel) {
    if (user.channelSettings(channel).ignored() || !channel.canPlayerUse(user)) {
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(channel.cannotUseMessage(),
        Template.of("color", "<" + channel.channelColor(user).toString() + ">"),
        Template.of("channel", channel.name())));

      return false;
    }
    return true;
  }

}
