package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class IgnoreCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public IgnoreCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
    channelArguments.put("player", CarbonUtils.chatUserArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.ignore"))
      .executesPlayer(this::execute)
      .register();
  }

  private void execute(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser targetUser = (ChatUser) args[0];
    final ChatUser user = this.carbonChat.userService().wrap(player);

    if (user.ignoringUser(targetUser)) {
      user.ignoringUser(targetUser, false);
      user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player,
        this.carbonChat.language().getString("not-ignoring-user"),
        "br", "\n", "player", targetUser.offlinePlayer().getName()));
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Permission permission = this.carbonChat.permission();
        final String format;

        if (permission.playerHas(null, targetUser.offlinePlayer(), "carbonchat.ignore.exempt")) {
          format = this.carbonChat.language().getString("ignore-exempt");
        } else {
          user.ignoringUser(targetUser, true);
          format = this.carbonChat.language().getString("ignoring-user");
        }

        final Component message = this.carbonChat.adventureManager().processMessageWithPapi(player, format,
          "br", "\n", "sender", player.getDisplayName(), "player",
          targetUser.offlinePlayer().getName());

        user.sendMessage(message);
      });

    }
  }

}
