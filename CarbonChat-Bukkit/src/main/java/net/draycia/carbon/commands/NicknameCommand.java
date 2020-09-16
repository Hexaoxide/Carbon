package net.draycia.carbon.commands;

import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class NicknameCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public NicknameCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> selfArguments = new LinkedHashMap<>();
    selfArguments.put("nickname", new StringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(selfArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.nickname"))
      .executesPlayer(this::executeSelf)
      .register();

    final LinkedHashMap<String, Argument> otherArguments = new LinkedHashMap<>();
    otherArguments.put("player", CarbonUtils.chatUserArgument());
    otherArguments.put("nickname", new StringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(otherArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.nickname.others"))
      .executes(this::executeOther)
      .register();
  }

  private void executeSelf(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    String nickname = (String) args[0];
    final ChatUser sender = this.carbonChat.userService().wrap(player.getUniqueId());

    if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase(player.getName())) {
      nickname = null;
    }

    sender.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().nicknameReset();
    } else {
      message = this.carbonChat.translations().nicknameSet();
    }

    sender.sendMessage(this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", Bukkit.getOfflinePlayer(sender.uuid()).getName()));
  }

  private void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    final ChatUser target = (ChatUser) args[0];
    String nickname = (String) args[1];

    if (nickname.equalsIgnoreCase("off") ||
      nickname.equalsIgnoreCase(Bukkit.getOfflinePlayer(target.uuid()).getName())) {
      nickname = null;
    }

    target.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().otherNicknameReset();
    } else {
      message = this.carbonChat.translations().otherNicknameSet();
    }

    cmdSender.sendMessage(this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", Bukkit.getOfflinePlayer(target.uuid()).getName()));
  }

}
