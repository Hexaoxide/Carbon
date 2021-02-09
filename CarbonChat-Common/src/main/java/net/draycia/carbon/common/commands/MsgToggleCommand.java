package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MsgToggleCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public MsgToggleCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("msgtoggle");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.msgtoggle")
        .handler(this::toggleSelf)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // console & player
        .permission("carbonchat.msgtoggle.others")
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name())) // carbonchat.channellist.other
        .handler(this::toggleOther)
        .build()
    );
  }

  private void toggleSelf(final @NonNull CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();

    final String message;

    final UserChannelSettings settings = user.channelSettings().get("whisper");

    if (settings == null) {
      throw new IllegalArgumentException("PlayerUser implementation failed to return settings for 'whisper'.");
    }

    if (settings.ignored()) {
      settings.ignoring(false);
      message = this.carbonChat.carbonSettings().whisperOptions().toggleOffMessage();
    } else {
      settings.ignoring(true);
      message = this.carbonChat.carbonSettings().whisperOptions().toggleOnMessage();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));
  }

  private void toggleOther(final @NonNull CommandContext<CarbonUser> context) {
    final CarbonUser sender = context.getSender();
    final PlayerUser user = context.get("user");

    final String message;
    final String otherMessage;

    final UserChannelSettings settings = user.channelSettings().get("whisper");

    if (settings == null) {
      throw new IllegalArgumentException("PlayerUser implementation failed to return settings for 'whisper'.");
    }

    if (settings.ignored()) {
      settings.ignoring(false);
      message = this.carbonChat.carbonSettings().whisperOptions().toggleOffMessage();
      otherMessage = this.carbonChat.carbonSettings().whisperOptions().toggleOtherOffMessage();
    } else {
      settings.ignoring(true);
      message = this.carbonChat.carbonSettings().whisperOptions().toggleOnMessage();
      otherMessage = this.carbonChat.carbonSettings().whisperOptions().toggleOtherOnMessage();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));

    sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(otherMessage,
      "player", user.name()));
  }
}
