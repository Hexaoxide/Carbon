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
import net.draycia.carbon.common.utils.ColorUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NicknameCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public NicknameCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("nickname");

    if (commandSettings == null || !commandSettings.enabled()) {
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
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name())) // carbonchat.channellist.other
        .handler(this::nicknameOther)
        .build()
    );
  }

  private void nicknameSelf(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    String nickname = context.get("nickname");

    if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase(user.username())) {
      nickname = null;
    }

    if (nickname != null) {
      nickname = ColorUtils.translateAlternateColors(nickname);
    }

    user.nickname(this.carbonChat.messageProcessor().processMessage(nickname));

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().nicknameReset();
    } else {
      message = this.carbonChat.translations().nicknameSet();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message,
      Template.of("nickname", nickname == null ? "" : nickname),
      Template.of("user", user.name()), Template.of("sender", context.getSender().name())));
  }

  private void nicknameOther(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser target = context.get("user");
    String nickname = context.get("nickname");

    if (nickname.equalsIgnoreCase("off") ||
      nickname.equalsIgnoreCase(target.username())) {
      nickname = null;
    }

    target.nickname(this.carbonChat.messageProcessor().processMessage(nickname));

    final String message;

    if (nickname == null) {
      message = this.carbonChat.translations().otherNicknameReset();
    } else {
      message = this.carbonChat.translations().otherNicknameSet();
    }

    context.getSender().sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message,
      Template.of("nickname", nickname == null ? "" : nickname),
      Template.of("user", target.name()), Template.of("sender", context.getSender().name())));
  }

}
