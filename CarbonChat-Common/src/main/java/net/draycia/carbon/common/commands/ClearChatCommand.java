package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
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
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.clearchat.clear")
        .handler(this::clearChat)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.clearchat.clear.player")
        .argument(PlayerUserArgument.requiredPlayerUserArgument())
        .handler(this::clearChatPlayer)
        .build()
    );
  }

  private void clearChat(@NonNull final CommandContext<CarbonUser> context) {
    final String sender = context.getSender().name();
    final String format = this.carbonChat.moderationSettings().clearChat().message();
    final Component component = this.carbonChat.messageProcessor().processMessage(format, "br", "\n");

    for (final CarbonUser user : this.carbonChat.userService().onlineUsers()) {
      this.clearUserChat(user, component, sender);
    }
  }

  private void clearChatPlayer(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser target = context.get("user");
    final String sender = context.getSender().name();
    final String format = this.carbonChat.moderationSettings().clearChat().message();
    final Component component = this.carbonChat.messageProcessor().processMessage(format, "br", "\n");

    this.clearUserChat(target, component, sender);
  }

  private void clearUserChat(@NonNull final CarbonUser user, @NonNull final Component component, @NonNull final String sender) {
    if (user.hasPermission("carbonchat.clearchat.exempt")) {
      final String message = this.carbonChat.translations().clearExempt();
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
        .processMessage(message, "player", sender));
    } else {
      for (int i = 0; i < this.carbonChat.moderationSettings().clearChat().messageCount(); i++) {
        user.sendMessage(Identity.nil(), component);
      }
    }

    if (user.hasPermission("carbonchat.clearchat.notify")) {
      final String message = this.carbonChat.translations().clearNotify();
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
        .processMessage(message, "player", sender));
    }
  }

}
