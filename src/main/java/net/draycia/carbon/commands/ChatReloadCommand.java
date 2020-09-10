package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChatReloadCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ChatReloadCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    new CommandAPICommand(commandSettings.getName())
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.reload"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull CommandSender sender, @NonNull Object @NonNull [] args) {
    carbonChat.reloadConfig();
    carbonChat.reloadFilters();

    Component message = carbonChat.getAdventureManager().processMessage(carbonChat.getLanguage().getString("reloaded"),
      "br", "\n");

    carbonChat.getAdventureManager().getAudiences().audience(sender).sendMessage(message);
  }

}
