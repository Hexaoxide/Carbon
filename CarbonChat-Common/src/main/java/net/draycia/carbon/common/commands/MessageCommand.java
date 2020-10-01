package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.channels.CarbonWhisperChannel;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class MessageCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public MessageCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();
    final CommandSettings commandSettings = carbonChat.commandSettings().get("message");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.message")
        .argument(CommandUtils.chatUserArgument())
        .argument(StringArgument.<ChatUser>newBuilder("message").greedy().asOptional().build())
        .handler(this::sendMessage)
        .build()
    );
  }

  private void sendMessage(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser receiver = context.getRequired("target");
    final Optional<String> message = context.get("message");

    if (message.isPresent()) {
      receiver.sendMessage(sender, message.get());
    } else {
      sender.selectedChannel(new CarbonWhisperChannel(sender, receiver));
    }
  }

}
