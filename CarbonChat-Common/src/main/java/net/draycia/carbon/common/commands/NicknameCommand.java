package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.components.standard.StringComponent;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NicknameCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public NicknameCommand(@NonNull final CommandManager<ChatUser, SimpleCommandMeta> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("nickname");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.nickname")
        .component(StringComponent.<ChatUser>newBuilder("nickname").single().build())
        .handler(this::nicknameOther)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.nickname")
        .component(CommandUtils.chatUserComponent())
        .component(StringComponent.<ChatUser>newBuilder("nickname").single().build())
        .handler(this::nicknameOther)
        .build()
    );

    selfArguments.put("nickname", new StringArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(selfArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.nickname"))
      .executesPlayer(this::executeSelf)
      .register();
  }

  private void nicknameSelf(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
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
      "user", user.name()));
  }

  private void nicknameOther(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser target = context.getRequired("user");
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
      "user", target.name()));
  }

}
