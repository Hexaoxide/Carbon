package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class IgnoreCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public IgnoreCommand(@NonNull final CommandManager<ChatUser, SimpleCommandMeta> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettingsRegistry().get("ignore");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // console & player
        .withPermission("carbonchat.ignore")
        .component(CommandUtils.chatUserComponent())
        .handler(this::ignoreUser)
        .build()
    );
  }

  private void ignoreUser(@NonNull final CommandContext<ChatUser> context) {
    final ChatUser sender = context.getSender();
    final ChatUser targetUser = context.getRequired("user");

    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(sender.uuid());

    if (sender.ignoringUser(targetUser)) {
      sender.ignoringUser(targetUser, false);
      sender.sendMessage(this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.translations().notIgnoringUser(),
        "br", "\n", "player", offlinePlayer.getName()));
    } else {
      Bukkit.getScheduler().runTaskAsynchronously(this.carbonChat, () -> {
        final Permission permission = this.carbonChat.permission();
        final String format;

        if (permission.playerHas(null, offlinePlayer, "carbonchat.ignore.exempt")) {
          format = this.carbonChat.translations().ignoreExempt();
        } else {
          user.ignoringUser(targetUser, true);
          format = this.carbonChat.translations().ignoringUser();
        }

        final Component message = this.carbonChat.messageProcessor().processMessage(format,
          "br", "\n", "sender", player.getDisplayName(), "player", offlinePlayer.getName());

        user.sendMessage(message);
      });

    }
  }

}
