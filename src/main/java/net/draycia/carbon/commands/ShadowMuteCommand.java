package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class ShadowMuteCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ShadowMuteCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
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
    final ChatUser user = (ChatUser) args[0];
    final Audience audience = this.carbonChat.adventureManager().audiences().audience(sender);

    if (user.shadowMuted()) {
      user.shadowMuted(false);
      final String format = this.carbonChat.language().getString("no-longer-shadow-muted");

      final Component message = this.carbonChat.adventureManager().processMessage(format, "br", "\n",
        "player", user.offlinePlayer().getName());

      audience.sendMessage(message);
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Permission permission = this.carbonChat.permission();
        final String format;

        if (permission.playerHas(null, user.offlinePlayer(), "carbonchat.shadowmute.exempt")) {
          format = this.carbonChat.language().getString("shadow-mute-exempt");
        } else {
          user.shadowMuted(true);
          format = this.carbonChat.language().getString("is-now-shadow-muted");
        }

        final Component message = this.carbonChat.adventureManager().processMessage(format, "br", "\n",
          "player", user.offlinePlayer().getName());

        audience.sendMessage(message);
      });
    }
  }
}
