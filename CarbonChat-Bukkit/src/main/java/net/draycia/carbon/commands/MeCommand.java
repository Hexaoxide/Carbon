package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class MeCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public MeCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
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
    String format = PlaceholderAPI.setPlaceholders(player, this.carbonChat.translations().roleplayFormat());

    if (!player.hasPermission("carbonchat.me.formatting")) {
      format = format.replace("<message>", "<pre><message></pre>");
    }

    final Component component = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
      "displayname", player.getDisplayName(), "message", message);

    final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());

    if (user.shadowMuted()) {
      user.sendMessage(component);
    } else {
      for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (this.carbonChat.userService().wrap(onlinePlayer.getUniqueId()).ignoringUser(user)) {
          continue;
        }

        this.carbonChat.messageProcessor().audiences().player(onlinePlayer.getUniqueId()).sendMessage(component);
      }
    }
  }

}
