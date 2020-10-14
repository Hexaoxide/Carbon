package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MuteCommand {

  private @NonNull final CarbonChat carbonChat;

  public MuteCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("mute");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.mute")
        .argument(PlayerUserArgument.requiredPlayerUserArgument())
        .handler(this::mute)
        .build()
    );
  }

  private void mute(@NonNull final CommandContext<CarbonUser> context) {
    final CarbonUser user = context.getSender();
    final PlayerUser target = context.get("user");

    if (target.muted()) {
      target.muted(false);
      final String format = this.carbonChat.translations().noLongerMuted();

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        "player", target.name(), "sender", user.name());

      user.sendMessage(Identity.nil(), message);
    } else {
      // TODO: schedule task because LuckPerms doesn't like sync offline permission checks
      final String format;

      if (target.hasPermission("carbonchat.mute.exempt")) {
        format = this.carbonChat.translations().muteExempt();
      } else {
        target.muted(true);
        format = this.carbonChat.translations().nowMuted();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        "player", target.name(), "sender", user.name());

      user.sendMessage(Identity.nil(), message);
    }
  }
}
