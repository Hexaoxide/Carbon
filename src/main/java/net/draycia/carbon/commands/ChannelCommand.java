package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ChannelCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final List<Argument> setChannelArguments = new ArrayList<>();
    setChannelArguments.add(CarbonUtils.channelArgument("channel"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(setChannelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel"))
      .executesPlayer(this::channel)
      .register();

    final List<Argument> sendMessageArguments = new ArrayList<>();
    sendMessageArguments.add(CarbonUtils.channelArgument("channel"));
    sendMessageArguments.add(new GreedyStringArgument("message"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(sendMessageArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel.message"))
      .executesPlayer(this::sendMessage)
      .register();
  }

  private void channel(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);
    final ChatChannel channel = (ChatChannel) args[0];

    if (user.channelSettings(channel).ignored()) {
      user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, channel.cannotUseMessage(),
        "br", "\n",
        "color", "<" + channel.channelColor(user).toString() + ">",
        "channel", channel.name()));

      return;
    }

    user.selectedChannel(channel);
  }

  private void sendMessage(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);
    final ChatChannel channel = (ChatChannel) args[0];
    final String message = (String) args[1];

    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.adventureManager().audiences().console().sendMessage(component);
  }

}
