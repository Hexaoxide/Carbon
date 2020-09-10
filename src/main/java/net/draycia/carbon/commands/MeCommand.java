package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class MeCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public MeCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
    channelArguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.me"))
      .executesPlayer(this::execute)
      .register();
  }

  private void execute(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final String message = ((String) args[0]).replace("</pre>", "");
    String format = PlaceholderAPI.setPlaceholders(player, this.carbonChat.language().getString("me"));

    if (!player.hasPermission("carbonchat.me.formatting")) {
      format = format.replace("<message>", "<pre><message></pre>");
    }

    final Component component = this.carbonChat.adventureManager().processMessage(format, "br", "\n",
      "displayname", player.getDisplayName(), "message", message);

    final ChatUser user = this.carbonChat.userService().wrap(player);

    if (user.shadowMuted()) {
      user.sendMessage(component);
    } else {
      for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (this.carbonChat.userService().wrap(onlinePlayer).ignoringUser(user)) {
          continue;
        }

        this.carbonChat.adventureManager().audiences().player(onlinePlayer).sendMessage(component);
      }
    }
  }

}
