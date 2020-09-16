package net.draycia.carbon.commands;

import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChatBukkit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ShadowMuteCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public ShadowMuteCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
    arguments.put("player", CarbonUtils.chatUserArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(arguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.shadowmute"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    final ChatUser user = (ChatUser) args[0];

    final OfflinePlayer player = Bukkit.getOfflinePlayer(user.uuid());

    if (user.shadowMuted()) {
      user.shadowMuted(false);
      final String format = this.carbonChat.translations().noLongerShadowMuted();

      final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
        "player", player.getName());

      cmdSender.sendMessage(message);
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Permission permission = this.carbonChat.permission();
        final String format;

        if (permission.playerHas(null, player, "carbonchat.shadowmute.exempt")) {
          format = this.carbonChat.translations().shadowMuteExempt();
        } else {
          user.shadowMuted(true);
          format = this.carbonChat.translations().nowShadowMuted();
        }

        final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
          "player", player.getName());

        cmdSender.sendMessage(message);
      });
    }
  }
}
