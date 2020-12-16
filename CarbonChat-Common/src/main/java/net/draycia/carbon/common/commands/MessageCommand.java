package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.channels.CarbonWhisperChannel;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class MessageCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public MessageCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();
    final CommandSettings commandSettings = carbonChat.commandSettings().get("message");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.message")
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name()))
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().asOptional().build())
        .handler(this::sendMessage)
        .build()
    );
  }

  private void sendMessage(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser sender = (PlayerUser) context.getSender();
    final PlayerUser receiver = context.get("user");

    if (sender.equals(receiver)) {
      final String message = this.carbonChat.translations().cannotWhisperSelf();
      sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));

      return;
    }

    if (!sender.hasPermission("carbonchat.msgtoggle.bypass")) {
      final ChatChannel senderWhisperChannel = new CarbonWhisperChannel(sender, receiver);
      final UserChannelSettings senderSettings = sender.channelSettings(senderWhisperChannel);

      if (senderSettings.ignored()) {
        final String message = this.carbonChat.carbonSettings().whisperOptions().senderToggledOff();
        sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));
        return;
      }

      final ChatChannel receiverWhisperChannel = new CarbonWhisperChannel(receiver, sender);
      final UserChannelSettings receiverSettings = sender.channelSettings(receiverWhisperChannel);

      if (receiverSettings.ignored()) {
        final String message = this.carbonChat.carbonSettings().whisperOptions().receiverToggledOff();
        sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));
        return;
      }
    }

    final Optional<String> message = context.getOptional("message");

    if (message.isPresent()) {
      receiver.sendMessage(sender, message.get());
    } else {
      sender.selectedChannel(new CarbonWhisperChannel(sender, receiver));

      sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.carbonSettings().whisperOptions().nowWhisperingPlayer(),
        "player", receiver.name()
      ));
    }
  }

}
