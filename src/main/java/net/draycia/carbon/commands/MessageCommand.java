package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class MessageCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public MessageCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
    arguments.put("player", CarbonUtils.chatUserArgument());
    arguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.getName())
      .withArguments(arguments)
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.message"))
      .executesPlayer(this::execute)
      .register();
  }

  private void execute(@NonNull Player player, @NonNull Object @NonNull [] args) {
    ChatUser targetUser = (ChatUser) args[0];
    String message = (String) args[1];

    ChatUser sender = carbonChat.getUserService().wrap(player);

    targetUser.sendMessage(sender, message);
  }

}
