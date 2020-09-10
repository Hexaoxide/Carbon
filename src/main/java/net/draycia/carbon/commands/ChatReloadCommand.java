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

  public ChatReloadCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
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

    final Component message = this.carbonChat.getAdventureManager()
      .processMessage(this.carbonChat.getLanguage().getString("reloaded"), "br", "\n");

    this.carbonChat.getAdventureManager().audiences().audience(sender).sendMessage(message);
  }

}
