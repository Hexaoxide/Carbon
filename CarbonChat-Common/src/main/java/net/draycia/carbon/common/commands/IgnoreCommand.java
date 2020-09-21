package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class IgnoreCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public IgnoreCommand(@NonNull final CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("ignore");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // console & player
        .withPermission("carbonchat.ignore")
        .argument(CommandUtils.chatUserArgument())
        .handler(this::ignoreUser)
        .build()
    );
  }

  private void ignoreUser(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser targetUser = context.getRequired("user");

    if (sender.ignoringUser(targetUser)) {
      sender.ignoringUser(targetUser, false);

      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.translations().notIgnoringUser(),
        "br", "\n", "player", targetUser.displayName()));
    } else {
      // TODO: schedule task because sync permission checks
      final String format;

      if (sender.hasPermission("carbonchat.ignore.exempt")) {
        format = this.carbonChat.translations().ignoreExempt();
      } else {
        sender.ignoringUser(targetUser, true);
        format = this.carbonChat.translations().ignoringUser();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        "br", "\n", "sender", sender.displayName(), "player", targetUser.displayName());

      sender.sendMessage(message);
    }
  }

}
