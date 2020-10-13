package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ClearChatCommand {

  private @NonNull final CarbonChat carbonChat;

  public ClearChatCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("clearchat");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(CarbonUser.class) // player & console
        .withPermission("carbonchat.clearchat.clear")
        .handler(this::clearChat)
        .build()
    );
  }

  private void clearChat(@NonNull final CommandContext<CarbonUser> context) {
    final String sender = context.getSender().name();
    final String format = this.carbonChat.moderationSettings().clearChat().message();
    final Component component = this.carbonChat.messageProcessor().processMessage(format, "br", "\n");

    for (final CarbonUser user : this.carbonChat.userService().onlineUsers()) {
      if (user.hasPermission("carbonchat.clearchat.exempt")) {
        final String message = this.carbonChat.translations().clearExempt();
        user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "player", sender));
      } else {
        for (int i = 0; i < this.carbonChat.moderationSettings().clearChat().messageCount(); i++) {
          user.sendMessage(component);
        }
      }

      if (user.hasPermission("carbonchat.clearchat.notify")) {
        final String message = this.carbonChat.translations().clearNotify();
        user.sendMessage(this.carbonChat.messageProcessor().processMessage(message, "player", sender));
      }
    }
  }

}
