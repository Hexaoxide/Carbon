package net.draycia.carbon.commands;

import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class SudoChannelCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public SudoChannelCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
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

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));

    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    cmdSender.sendMessage(
      this.carbonChat.messageProcessor().processMessage(otherMessage, "br", "\n",
        "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name(),
        "player", Bukkit.getOfflinePlayer(user.uuid()).getName()));
  }

  private void sendMessageOther(@NonNull final CommandSender sender, @NonNull final ChatUser user,
                                @NonNull final ChatChannel channel, @NonNull final String message) {
    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.messageProcessor().audiences().console().sendMessage(component);
  }

}
