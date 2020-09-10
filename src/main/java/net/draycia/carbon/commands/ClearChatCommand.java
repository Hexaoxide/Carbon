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

  public ClearChatCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    new CommandAPICommand(commandSettings.getName())
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.clearchat.clear"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull CommandSender sender, @NonNull Object @NonNull [] args) {
    String format = carbonChat.getModConfig().getString("clear-chat.message", "");
    Component component = carbonChat.getAdventureManager().processMessage(format, "br", "\n");

    for (Player player : Bukkit.getOnlinePlayers()) {
      ChatUser audience = carbonChat.getUserService().wrap(player);

      if (player.hasPermission("carbonchat.clearchat.exempt")) {
        String message = carbonChat.getLanguage().getString("clear-exempt");
        audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
      } else {
        for (int i = 0; i < carbonChat.getModConfig().getInt("clear-chat.message-count", 100); i++) {
          audience.sendMessage(component);
        }
      }

      if (player.hasPermission("carbonchat.clearchat.notify")) {
        String message = carbonChat.getLanguage().getString("clear-notify");
        audience.sendMessage(carbonChat.getAdventureManager().processMessage(message, "player", sender.getName()));
      }
    }
  }

}
