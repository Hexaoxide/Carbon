package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.utils.CommandUtils;
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
        .withSenderType(CarbonUser.class) // player & console
        .withPermission("carbonchat.mute")
        .argument(CommandUtils.chatUserArgument())
        .handler(this::mute)
        .build()
    );
  }

  private void mute(@NonNull final CommandContext<CarbonUser> context) {
    final CarbonUser user = context.getSender();
    final PlayerUser target = context.getRequired("user");

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
