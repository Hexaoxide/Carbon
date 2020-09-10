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

  public ShadowMuteCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
    arguments.put("player", CarbonUtils.chatUserArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(arguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.shadowmute"))
      .executes(this::execute)
      .register();
  }

  private void execute(@NonNull CommandSender sender, @NonNull Object @NonNull [] args) {
    ChatUser user = (ChatUser) args[0];
    Audience audience = carbonChat.getAdventureManager().getAudiences().audience(sender);

    if (user.isShadowMuted()) {
      user.setShadowMuted(false);
      String format = carbonChat.getLanguage().getString("no-longer-shadow-muted");

      Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
        "player", user.offlinePlayer().getName());

      audience.sendMessage(message);
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
        Permission permission = carbonChat.getPermission();
        String format;

        if (permission.playerHas(null, user.offlinePlayer(), "carbonchat.shadowmute.exempt")) {
          format = carbonChat.getLanguage().getString("shadow-mute-exempt");
        } else {
          user.setShadowMuted(true);
          format = carbonChat.getLanguage().getString("is-now-shadow-muted");
        }

        Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
          "player", user.offlinePlayer().getName());

        audience.sendMessage(message);
      });
    }
  }
}
