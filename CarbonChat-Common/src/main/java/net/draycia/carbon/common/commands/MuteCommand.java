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

public class MuteCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public MuteCommand(@NonNull final CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("mute");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.mute")
        .argument(CommandUtils.chatUserArgument())
        .handler(this::mute)
        .build()
    );
  }

  private void mute(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatUser target = context.getRequired("user");

    if (user.muted()) {
      user.muted(false);
      final String format = this.carbonChat.translations().noLongerMuted();

      final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
        "player", target.name());

      user.sendMessage(message);
    } else {
      // TODO: schedule task because LuckPerms doesn't like sync offline permission checks
      final String format;

      if (target.hasPermission("carbonchat.mute.exempt")) {
        format = this.carbonChat.translations().muteExempt();
      } else {
        user.muted(true);
        format = this.carbonChat.translations().nowMuted();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format, "br", "\n",
        "player", target.name());

      user.sendMessage(message);
    }
  }
}
