package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.meta.CommandMeta;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChatReloadCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ChatReloadCommand(@NonNull final CommandManager<ChatUser, SimpleCommandMeta> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("chatreload");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player & console
        .withPermission("carbonchat.reload")
        .handler(this::reloadConfig)
        .build()
    );
  }

  private void reloadConfig(@NonNull final CommandContext<ChatUser> context) {
    this.carbonChat.reloadConfig();
    this.carbonChat.reloadFilters();

    final Component message = this.carbonChat.messageProcessor()
      .processMessage(this.carbonChat.translations().reloaded().replace("br", "\n"));

    context.getSender().sendMessage(message);
  }

}
