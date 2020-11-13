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
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SudoChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SudoChannelCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final List<Argument> setOtherChannelArguments = new ArrayList<>();
    setOtherChannelArguments.add(CarbonUtils.onlineChatUserArgument("player"));
    setOtherChannelArguments.add(CarbonUtils.channelArgument("channel"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(setOtherChannelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel.others"))
      .executes((sender, args) -> {
        this.otherChannel(sender, (ChatUser) args[0], (ChatChannel) args[1]);
      })
      .register();

    final List<Argument> sendMessageOtherArguments = new ArrayList<>();
    sendMessageOtherArguments.add(CarbonUtils.onlineChatUserArgument("player"));
    sendMessageOtherArguments.add(CarbonUtils.channelArgument("channel"));
    sendMessageOtherArguments.add(new GreedyStringArgument("message"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(sendMessageOtherArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel.others.message"))
      .executes((sender, args) -> {
        this.sendMessageOther(sender, (ChatUser) args[0], (ChatChannel) args[1], (String) args[2]);
      })
      .register();
  }

  private void otherChannel(@NonNull final CommandSender sender, @NonNull final ChatUser user, @NonNull final ChatChannel channel) {
    user.selectedChannel(channel);

    final String message = channel.switchMessage();
    final String otherMessage = channel.switchOtherMessage();

    user.sendMessage(this.carbonChat.adventureManager().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));

    this.carbonChat.adventureManager().audiences().sender(sender).sendMessage(
      this.carbonChat.adventureManager().processMessage(otherMessage, "br", "\n",
        "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name(),
        "player", user.offlinePlayer().getName()));
  }

  private void sendMessageOther(@NonNull final CommandSender sender, @NonNull final ChatUser user,
                                @NonNull final ChatChannel channel, @NonNull final String message) {
    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.adventureManager().audiences().console().sendMessage(component);
  }

}
