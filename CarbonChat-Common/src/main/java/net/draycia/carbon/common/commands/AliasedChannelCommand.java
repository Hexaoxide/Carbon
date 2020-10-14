package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
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
      commandManager.commandBuilder(this.commandName,
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.channel") // carbonchat.channel.message
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().asOptional().build())
        .handler(context -> {
          if (context.getOptional("message").isPresent()) {
            this.sendMessage(context);
          } else {
            this.channel(context);
          }
        })
        .build() // TODO: use chain to register two executors
    );
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
    context.<String>getOptional("message").ifPresent(message -> {
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
