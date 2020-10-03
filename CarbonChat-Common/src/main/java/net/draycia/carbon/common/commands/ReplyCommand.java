package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReplyCommand {

  private final @NonNull CarbonChat carbonChat;

  public ReplyCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("reply");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.reply")
        .argument(StringArgument.<ChatUser>newBuilder("message").greedy().build())
        .handler(this::reply)
        .build()
    );
  }

  private void reply(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final String input = context.getRequired("message");

    if (input.isEmpty()) {
      final String message = this.carbonChat.translations().replyMessageBlank();
      final Component component = this.carbonChat.messageProcessor().processMessage(message, "br", "\n");

      user.sendMessage(component);

      return;
    }

    if (user.replyTarget() == null) {
      final String message = this.carbonChat.translations().noReplyTarget();
      final Component component = this.carbonChat.messageProcessor().processMessage(message, "br", "\n");

      user.sendMessage(component);

      return;
    }

    final ChatUser targetUser = this.carbonChat.userService().wrap(user.replyTarget());

    targetUser.sendMessage(user, input);
  }

}
