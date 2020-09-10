package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ClearChatCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ClearChatCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    new CommandAPICommand(commandSettings.name())
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.clearchat.clear"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final String format = this.carbonChat.getModConfig().getString("clear-chat.message", "");
    final Component component = this.carbonChat.getAdventureManager().processMessage(format, "br", "\n");

    for (final Player player : Bukkit.getOnlinePlayers()) {
      final ChatUser audience = this.carbonChat.getUserService().wrap(player);

      if (player.hasPermission("carbonchat.clearchat.exempt")) {
        final String message = this.carbonChat.getLanguage().getString("clear-exempt");
        audience.sendMessage(this.carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
      } else {
        for (int i = 0; i < this.carbonChat.getModConfig().getInt("clear-chat.message-count", 100); i++) {
          audience.sendMessage(component);
        }
      }

      if (player.hasPermission("carbonchat.clearchat.notify")) {
        final String message = this.carbonChat.getLanguage().getString("clear-notify");
        audience.sendMessage(this.carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
      }
    }
  }

}
