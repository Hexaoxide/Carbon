package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NicknameCommand {

  private @NonNull final CarbonChat carbonChat;

  public NicknameCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("nickname");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.nickname")
        .argument(StringArgument.of("nickname"))
        .handler(this::nicknameSelf)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // console & player
        .permission("carbonchat.nickname.others")
        .argument(StringArgument.of("nickname"))
        .argument(PlayerUserArgument.requiredPlayerUserArgument()) // carbonchat.channellist.other
        .handler(this::nicknameOther)
        .build()
    );
  }

  private void nicknameSelf(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    String nickname = context.get("nickname");

    if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase(user.name())) {
      nickname = null;
    }

    user.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().nicknameReset();
    } else {
      message = this.carbonChat.translations().nicknameSet();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", user.name(), "sender", context.getSender().name()));
  }

  private void nicknameOther(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser target = context.get("user");
    String nickname = context.get("nickname");

    if (nickname.equalsIgnoreCase("off") ||
      nickname.equalsIgnoreCase(target.name())) {
      nickname = null;
    }

    target.nickname(nickname);

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().otherNicknameReset();
    } else {
      message = this.carbonChat.translations().otherNicknameSet();
    }

    context.getSender().sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", target.name(), "sender", context.getSender().name()));
  }

}
