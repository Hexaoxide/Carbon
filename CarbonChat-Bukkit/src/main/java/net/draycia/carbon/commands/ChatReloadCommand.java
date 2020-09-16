package net.draycia.carbon.commands;

import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.draycia.carbon.CarbonChatBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChatReloadCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public ChatReloadCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    new CommandAPICommand(commandSettings.name())
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.reload"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    this.carbonChat.reloadConfig();
    this.carbonChat.reloadFilters();

    final Component message = this.carbonChat.messageProcessor()
      .processMessage(this.carbonChat.translations().reloaded().replace("br", "\n"));

    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    cmdSender.sendMessage(message);
  }

}
