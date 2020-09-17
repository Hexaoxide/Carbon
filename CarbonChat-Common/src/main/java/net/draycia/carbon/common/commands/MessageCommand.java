package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.components.standard.StringComponent;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MessageCommand {

  public MessageCommand(@NonNull final CommandManager<ChatUser, SimpleCommandMeta> commandManager, @NonNull final CommandSettings commandSettings) {
    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.message")
        .component(CommandUtils.chatUserComponent())
        .component(StringComponent.<ChatUser>newBuilder("message").greedy().build())
        .handler(this::sendMessage)
        .build()
    );
  }

  private void sendMessage(@NonNull final CommandContext<ChatUser> context) {
    context.<ChatUser>getRequired("target")
      .sendMessage(context.getSender(), context.getRequired("message"));
  }

}
