package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AliasedChannelCommand {

  private @NonNull final CarbonChat carbonChat;

  private @NonNull final TextChannel chatChannel;

  private @NonNull final String commandName;

  public AliasedChannelCommand(@NonNull final CommandManager<CarbonUser> commandManager, @NonNull final TextChannel chatChannel) {
    this.carbonChat = CarbonChatProvider.carbonChat();
    this.chatChannel = chatChannel;
    this.commandName = chatChannel.key();

    commandManager.command(
      commandManager.commandBuilder(this.commandName)
        .withSenderType(PlayerUser.class) // player
        .withPermission("carbonchat.channel")
        .handler(this::channel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(this.commandName)
        .withSenderType(PlayerUser.class) // player
        .withPermission("carbonchat.channel.message")
        .argument(StringArgument.required("message"))
        .handler(this::sendMessage)
        .build()
    );

    // TODO: don't register same command twice lol
  }

  private void channel(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();

    if (user.channelSettings(this.chatChannel()).ignored()) {
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(this.chatChannel().cannotUseMessage(),
        "color", "<" + this.chatChannel().channelColor(user).toString() + ">",
        "channel", this.chatChannel().name()));

      return;
    }

    user.selectedChannel(this.chatChannel());
  }

  private void sendMessage(@NonNull final CommandContext<CarbonUser> context) {
    context.<String>get("message").ifPresent(message -> {
      this.chatChannel().sendComponentsAndLog(
        this.chatChannel().parseMessage((PlayerUser) context.getSender(), message, false));
    });
  }

  public @NonNull TextChannel chatChannel() {
    return this.chatChannel;
  }

  public @NonNull String commandName() {
    return this.commandName;
  }
}
