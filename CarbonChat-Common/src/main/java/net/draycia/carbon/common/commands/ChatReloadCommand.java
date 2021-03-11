package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.api.users.CarbonUser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChatReloadCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public ChatReloadCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("chatreload");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.reload")
        .handler(this::reloadConfig)
        .build()
    );
  }

  private void reloadConfig(final @NonNull CommandContext<CarbonUser> context) {
    this.carbonChat.reload();

    final Component message = this.carbonChat.messageProcessor()
      .processMessage(this.carbonChat.translations().reloaded().replace("br", "\n"));

    context.getSender().sendMessage(Identity.nil(), message);
  }

}
