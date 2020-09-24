package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.common.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShadowMuteCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ShadowMuteCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("shadowmute");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.shadowmute")
        .argument(CommandUtils.chatUserArgument())
        .handler(this::shadowMute)
        .build()
    );
  }

  private void shadowMute(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatUser target = context.getRequired("user");

    if (user.shadowMuted()) {
      user.shadowMuted(false);
      final String format = this.carbonChat.translations().noLongerShadowMuted();

      final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
        "player", target.name());

      user.sendMessage(message);
    } else {
      // TODO: schedule task because LuckPerms doesn't like sync offline permission checks
      final String format;

      if (target.hasPermission("carbonchat.mute.exempt")) {
        format = this.carbonChat.translations().shadowMuteExempt();
      } else {
        user.shadowMuted(true);
        format = this.carbonChat.translations().nowShadowMuted();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
        "player", target.name());

      user.sendMessage(message);
    }
  }
}
