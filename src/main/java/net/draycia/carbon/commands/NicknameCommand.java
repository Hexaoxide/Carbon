package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NicknameCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public NicknameCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final List<Argument> selfArguments = new ArrayList<>();
    selfArguments.add(new StringArgument("nickname"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(selfArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.nickname"))
      .executesPlayer(this::executeSelf)
      .register();

    final List<Argument> otherArguments = new ArrayList<>();
    otherArguments.add(CarbonUtils.chatUserArgument("player"));
    otherArguments.add(new StringArgument("nickname"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(otherArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.nickname.others"))
      .executes(this::executeOther)
      .register();
  }

  private void executeSelf(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    String nickname = (String) args[0];
    final ChatUser sender = this.carbonChat.userService().wrap(player);

    if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase(player.getName())) {
      nickname = null;
    }

    sender.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.language().getString("nickname-reset");
    } else {
      message = this.carbonChat.language().getString("nickname-set");
    }

    sender.sendMessage(this.carbonChat.adventureManager().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", sender.offlinePlayer().getName()));
  }

  private void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience user = this.carbonChat.adventureManager().audiences().sender(sender);
    final ChatUser target = (ChatUser) args[0];
    String nickname = (String) args[1];

    if (nickname.equalsIgnoreCase("off") ||
      nickname.equalsIgnoreCase(target.offlinePlayer().getName())) {
      nickname = null;
    }

    target.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.language().getString("other-nickname-reset");
    } else {
      message = this.carbonChat.language().getString("other-nickname-set");
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", target.offlinePlayer().getName()));
  }

}
