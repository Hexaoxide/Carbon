package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AliasedChannelCommand {

  private final @NonNull CarbonChat carbonChat;

  private final @NonNull ChatChannel chatChannel;

  private final @NonNull String commandName;

  public AliasedChannelCommand(final @NonNull CommandManager<ChatUser> commandManager, final @NonNull ChatChannel chatChannel) {
    this.carbonChat = CarbonChatProvider.carbonChat();
    this.chatChannel = chatChannel;
    this.commandName = chatChannel.key();

    commandManager.command(
      commandManager.commandBuilder(this.commandName)
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel")
        .handler(this::channel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(this.commandName)
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel.message")
        .argument(StringArgument.required("message"))
        .handler(this::sendMessage)
        .build()
    );
  }

  private void channel(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();

    if (user.channelSettings(this.chatChannel()).ignored()) {
      user.sendMessage(this.carbonChat.messageProcessor().processMessage(this.chatChannel().cannotUseMessage(),
        "br", "\n",
        "color", "<" + this.chatChannel().channelColor(user).toString() + ">",
        "channel", this.chatChannel().name()));

      return;
    }

    user.selectedChannel(this.chatChannel());
  }

  private void sendMessage(final @NonNull CommandContext<ChatUser> context) {
    context.<String>get("message").ifPresent(message -> {
      final Component component = this.chatChannel().sendMessage(context.getSender(), message, false);

      this.carbonChat.messageProcessor().audiences().console().sendMessage(component);
    });
  }

  public @NonNull ChatChannel chatChannel() {
    return this.chatChannel;
  }

  public @NonNull String commandName() {
    return this.commandName;
  }
}
