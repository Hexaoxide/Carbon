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

  public MeCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
    channelArguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(commandSettings.getName())
      .withArguments(channelArguments)
      .withAliases(commandSettings.getAliasesArray())
      .withPermission(CommandPermission.fromString("carbonchat.me"))
      .executesPlayer(this::execute)
      .register();
  }

  private void execute(@NonNull Player player, @NonNull Object @NonNull [] args) {
    String message = ((String) args[0]).replace("</pre>", "");
    String format = PlaceholderAPI.setPlaceholders(player, carbonChat.getLanguage().getString("me"));

    if (!player.hasPermission("carbonchat.me.formatting")) {
      format = format.replace("<message>", "<pre><message></pre>");
    }

    Component component = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
      "displayname", player.getDisplayName(), "message", message);

    ChatUser user = carbonChat.getUserService().wrap(player);

    if (user.isShadowMuted()) {
      user.sendMessage(component);
    } else {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (carbonChat.getUserService().wrap(onlinePlayer).isIgnoringUser(user)) {
          continue;
        }

        carbonChat.getAdventureManager().getAudiences().player(onlinePlayer).sendMessage(component);
      }
    }
  }

}
