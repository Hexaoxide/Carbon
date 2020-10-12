package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.channels.CarbonWhisperChannel;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class MessageCommand {

  private @NonNull final CarbonChat carbonChat;

  public MessageCommand(@NonNull final CommandManager<ChatUser> commandManager) {
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

  private void sendMessage(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser receiver = context.getRequired("user");

    if (sender.equals(receiver)) {
      final String message = this.carbonChat.translations().cannotWhisperSelf();
      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(message));

      return;
    }

    final ChatChannel senderWhisperChannel = this.carbonChat.channelRegistry().get("whisper");
    final UserChannelSettings senderSettings = sender.channelSettings(senderWhisperChannel);

    if (senderSettings.ignored()) {
      final String message = this.carbonChat.carbonSettings().whisperOptions().senderToggledOff();
      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(message));
      return;
    }

    final ChatChannel receiverWhisperChannel = this.carbonChat.channelRegistry().get("whisper");
    final UserChannelSettings receiverSettings = sender.channelSettings(receiverWhisperChannel);

    if (receiverSettings.ignored()) {
      final String message = this.carbonChat.carbonSettings().whisperOptions().receiverToggledOff();
      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(message));
      return;
    }

    final Optional<String> message = context.get("message");

    if (message.isPresent()) {
      receiver.sendMessage(sender, message.get());
    } else {
      sender.selectedChannel(new CarbonWhisperChannel(sender, receiver));

      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.carbonSettings().whisperOptions().nowWhisperingPlayer(),
        "player", receiver.name()
      ));
    }
  }

}
