package net.draycia.carbon.commands;

import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class IgnoreCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public IgnoreCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
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
    final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());

    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.uuid());

    if (user.ignoringUser(targetUser)) {
      user.ignoringUser(targetUser, false);
      user.sendMessage(this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.translations().notIgnoringUser(),
        "br", "\n", "player", offlinePlayer.getName()));
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Permission permission = this.carbonChat.permission();
        final String format;

        if (permission.playerHas(null, offlinePlayer, "carbonchat.ignore.exempt")) {
          format = this.carbonChat.translations().ignoreExempt();
        } else {
          user.ignoringUser(targetUser, true);
          format = this.carbonChat.translations().ignoringUser();
        }

        final Component message = this.carbonChat.messageProcessor().processMessage(format,
          "br", "\n", "sender", player.getDisplayName(), "player", offlinePlayer.getName());

        user.sendMessage(message);
      });

    }
  }

}
