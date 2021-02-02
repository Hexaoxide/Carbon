package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class ReplyCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public ReplyCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("reply");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.reply")
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().build())
        .handler(this::reply)
        .build()
    );
  }

  private void reply(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final String input = context.get("message");

    if (input.isEmpty()) {
      final String message = this.carbonChat.translations().replyMessageBlank();
      final Component component = this.carbonChat.messageProcessor().processMessage(message, "br", "\n");

      user.sendMessage(Identity.nil(), component);

      return;
    }

    final UUID replyTarget = user.replyTarget();

    if (replyTarget == null) {
      final String message = this.carbonChat.translations().noReplyTarget();
      final Component component = this.carbonChat.messageProcessor().processMessage(message, "br", "\n");

      user.sendMessage(Identity.nil(), component);

      return;
    }

    this.carbonChat.userService().wrap(replyTarget).sendMessage(user, input);
  }

}
