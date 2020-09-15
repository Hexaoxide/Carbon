package net.draycia.carbon.commands;

import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class SudoChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SudoChannelCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> setOtherChannelArguments = new LinkedHashMap<>();
    setOtherChannelArguments.put("player", CarbonUtils.onlineChatUserArgument());
    setOtherChannelArguments.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(setOtherChannelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channel.others"))
      .executes((sender, args) -> {
        this.otherChannel(sender, (ChatUser) args[0], (ChatChannel) args[1]);
      })
      .register();

    final LinkedHashMap<String, Argument> sendMessageOtherArguments = new LinkedHashMap<>();
    sendMessageOtherArguments.put("player", CarbonUtils.onlineChatUserArgument());
    sendMessageOtherArguments.put("channel", CarbonUtils.channelArgument());
    sendMessageOtherArguments.put("message", new GreedyStringArgument());

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

    this.carbonChat.adventureManager().audiences().audience(sender).sendMessage(
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
