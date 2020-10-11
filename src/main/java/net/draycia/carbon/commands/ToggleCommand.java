package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ToggleCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ToggleCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
    channelArguments.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.toggle"))
      .executesPlayer(this::executeSelf)
      .register();

    final LinkedHashMap<String, Argument> argumentsOther = new LinkedHashMap<>();
    argumentsOther.put("players", CarbonUtils.chatUserArgument());
    argumentsOther.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(argumentsOther)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.toggle"))
      .executes(this::executeOther)
      .register();
  }

  private void executeSelf(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);
    final ChatChannel channel = (ChatChannel) args[0];

    final String message;

    final UserChannelSettings settings = user.channelSettings(channel);

    if (!channel.ignorable()) {
      message = channel.cannotIgnoreMessage();
    } else if (settings.ignored()) {
      settings.ignoring(false);
      message = channel.toggleOffMessage();
    } else {
      settings.ignoring(true);
      message = channel.toggleOnMessage();
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));
  }

  private void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final ChatUser user = (ChatUser) args[0];
    final ChatChannel channel = (ChatChannel) args[1];

    final String message;
    final String otherMessage;

    final UserChannelSettings settings = user.channelSettings(channel);

    if (settings.ignored()) {
      settings.ignoring(false);
      message = channel.toggleOffMessage();
      otherMessage = channel.toggleOtherOffMessage();
    } else {
      settings.ignoring(true);
      message = channel.toggleOnMessage();
      otherMessage = channel.toggleOtherOnMessage();
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessage(message, "br", "\n",
      "color", "<color:" + channel.channelColor(user).toString() + ">", "channel", channel.name()));

    this.carbonChat.adventureManager().audiences().sender(sender).sendMessage(
      this.carbonChat.adventureManager().processMessage(otherMessage,
        "br", "\n", "color", "<color:" + channel.channelColor(user).toString() + ">",
        "channel", channel.name(), "player", user.offlinePlayer().getName()));
  }
}
