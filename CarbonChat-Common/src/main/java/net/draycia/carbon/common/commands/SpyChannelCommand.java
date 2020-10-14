package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpyChannelCommand {

  private @NonNull final CarbonChat carbonChat;

  public SpyChannelCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("spy");

    if (!commandSettings.enabled()) {
      return;
    }

    // TODO: Fix
    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.spy")
        .argument(ChannelArgument.requiredChannelArgument())
        .handler(this::spyChannel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.spy")
        .argument(StaticArgument.of("whispers"))
        .handler(this::spyWhispers)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.spy")
        .argument(StaticArgument.of("everything"))
        .argument(BooleanArgument.of("enabled"))
        .handler(this::spyEverything)
        .build()
    );
  }

  private void spyChannel(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final ChatChannel chatChannel = context.get("channel");

    final String message;

    final UserChannelSettings settings = user.channelSettings(chatChannel);

    if (settings.spying()) {
      settings.spying(false);
      message = this.carbonChat.translations().spyToggledOff();
    } else {
      settings.spying(true);
      message = this.carbonChat.translations().spyToggledOn();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message,
      "color", "<color:" + chatChannel.channelColor(user).toString() + ">",
      "channel", chatChannel.name()));
  }

  private void spyWhispers(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();

    final String message;

    if (user.spyingWhispers()) {
      user.spyingWhispers(false);
      message = this.carbonChat.translations().spyWhispersOff();
    } else {
      user.spyingWhispers(true);
      message = this.carbonChat.translations().spyWhispersOn();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(message));
  }

  private void spyEverything(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final Boolean shouldSpy = context.get("enabled");

    final String message;

    if (shouldSpy) {
      user.spyingWhispers(true);

      for (final ChatChannel channel : this.carbonChat.channelRegistry()) {
        user.channelSettings(channel).spying(true);
      }

      message = this.carbonChat.translations().spyEverythingOff();
    } else {
      user.spyingWhispers(false);

      for (final ChatChannel channel : this.carbonChat.channelRegistry()) {
        user.channelSettings(channel).spying(false);
      }

      message = this.carbonChat.translations().spyEverythingOn();
    }

    user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
      .processMessage(message, "br", "\n"));
  }

}
