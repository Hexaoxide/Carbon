package net.draycia.carbon.commands;

import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ChannelCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public ChannelCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();
    setChannelArguments.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(setChannelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel"))
      .executesPlayer(this::channel)
      .register();

    final LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
    sendMessageArguments.put("channel", CarbonUtils.channelArgument());
    sendMessageArguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(sendMessageArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel.message"))
      .executesPlayer(this::sendMessage)
      .register();
  }

  private void channel(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());
    final ChatChannel channel = (ChatChannel) args[0];

    if (user.channelSettings(channel).ignored()) {
      user.sendMessage(this.carbonChat.messageProcessor().processMessage(channel.cannotUseMessage(),
        "br", "\n",
        "color", "<" + channel.channelColor(user).toString() + ">",
        "channel", channel.name()));

      return;
    }

    user.selectedChannel(channel);
  }

  private void sendMessage(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());
    final ChatChannel channel = (ChatChannel) args[0];
    final String message = (String) args[1];

    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.messageProcessor().audiences().console().sendMessage(component);
  }

}
