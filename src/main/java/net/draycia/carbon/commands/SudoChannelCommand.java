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

import java.util.LinkedHashMap;

public class SudoChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SudoChannelCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> setOtherChannelArguments = new LinkedHashMap<>();
    setOtherChannelArguments.put("player", CarbonUtils.onlineChatUserArgument());
    setOtherChannelArguments.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.getName())
      .withArguments(setOtherChannelArguments)
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.channel.others"))
      .executes((sender, args) -> {
        this.setOtherChannel(sender, (ChatUser) args[0], (ChatChannel) args[1]);
      })
      .register();

    LinkedHashMap<String, Argument> sendMessageOtherArguments = new LinkedHashMap<>();
    sendMessageOtherArguments.put("player", CarbonUtils.onlineChatUserArgument());
    sendMessageOtherArguments.put("channel", CarbonUtils.channelArgument());
    sendMessageOtherArguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.getName())
      .withArguments(sendMessageOtherArguments)
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.channel.others.message"))
      .executes((sender, args) -> {
        this.sendMessageOther(sender, (ChatUser) args[0], (ChatChannel) args[1], (String) args[2]);
      })
      .register();
  }

  private void setOtherChannel(@NonNull CommandSender sender, @NonNull ChatUser user, @NonNull ChatChannel channel) {
    user.setSelectedChannel(channel);

    String message = channel.getSwitchMessage();
    String otherMessage = channel.getSwitchOtherMessage();

    user.sendMessage(carbonChat.getAdventureManager().processMessage(message, "br", "\n",
      "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName()));

    carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(carbonChat.getAdventureManager().processMessage(otherMessage, "br", "\n",
      "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName(),
      "player", user.asOfflinePlayer().getName()));
  }

  private void sendMessageOther(@NonNull CommandSender sender, @NonNull ChatUser user, @NonNull ChatChannel channel, @NonNull String message) {
    Component component = channel.sendMessage(user, message, false);

    carbonChat.getAdventureManager().getAudiences().console().sendMessage(component);
  }

}
