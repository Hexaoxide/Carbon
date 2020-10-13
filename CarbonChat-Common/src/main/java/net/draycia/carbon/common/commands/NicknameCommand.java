package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.utils.CommandUtils;
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
        .withSenderType(CarbonUser.class) // player & console
        .withPermission("carbonchat.nickname")
        .argument(StringArgument.required("nickname"))
        .argument(CommandUtils.optionalChatUserArgument()) // carbonchat.nickname.other
        .handler(context -> {
          if (context.get("user").isPresent()) {
            this.nicknameOther(context);
          } else if (context.getSender() instanceof PlayerUser) {
            // TODO: better handling of this
            this.nicknameSelf(context);
          }
        })
        .build()
    );
  }

  private void nicknameSelf(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    String nickname = context.getRequired("nickname");

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

    user.sendMessage(this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", user.name(), "sender", context.getSender().name()));
  }

  private void nicknameOther(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser target = context.getRequired("user");
    String nickname = context.getRequired("nickname");

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

    context.getSender().sendMessage(this.carbonChat.messageProcessor().processMessage(
      message, "nickname", nickname == null ? "" : nickname,
      "user", target.name(), "sender", context.getSender().name()));
  }

}
