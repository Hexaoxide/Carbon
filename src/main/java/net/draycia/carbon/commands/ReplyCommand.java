package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ReplyCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ReplyCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
    arguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(arguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.reply"))
      .executesPlayer(this::execute)
      .register();
  }

  private void execute(@NonNull Player player, @NonNull Object @NonNull [] args) {
    String input = (String) args[0];

    ChatUser user = carbonChat.getUserService().wrap(player);

    if (input == null || input.isEmpty()) {
      String message = carbonChat.getLanguage().getString("reply-message-blank");
      Component component = carbonChat.getAdventureManager().processMessage(message, "br", "\n");
      user.sendMessage(component);
      return;
    }

    if (user.getReplyTarget() == null) {
      String message = carbonChat.getLanguage().getString("no-reply-target");
      Component component = carbonChat.getAdventureManager().processMessage(message, "br", "\n");
      user.sendMessage(component);
      return;
    }

    ChatUser targetUser = carbonChat.getUserService().wrap(user.getReplyTarget());

    targetUser.sendMessage(user, input);
  }

}
