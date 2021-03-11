package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public ShadowMuteCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("shadowmute");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // player & console
        .permission("carbonchat.shadowmute")
        .argument(PlayerUserArgument.requiredPlayerUserArgument(commandSettings.name()))
        .handler(this::shadowMute)
        .build()
    );
  }

  private void shadowMute(final @NonNull CommandContext<CarbonUser> context) {
    final CarbonUser user = context.getSender();
    final PlayerUser target = context.get("user");

    if (target.shadowMuted()) {
      target.shadowMuted(false);
      final String format = this.carbonChat.translations().noLongerShadowMuted();

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        Template.of("player", target.name()));

      user.sendMessage(Identity.nil(), message);
    } else {
      final String format;

      if (target.hasPermission("carbonchat.mute.exempt")) {
        format = this.carbonChat.translations().shadowMuteExempt();
      } else {
        target.shadowMuted(true);
        format = this.carbonChat.translations().nowShadowMuted();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        Template.of("player", target.name()));

      user.sendMessage(Identity.nil(), message);
    }
  }
}
