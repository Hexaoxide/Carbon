package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MeCommand {

  private @NonNull final CarbonChat carbonChat;

  public MeCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("me");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(PlayerUser.class) // player
        .withPermission("carbonchat.me")
        .argument(StringArgument.<CarbonUser>newBuilder("message").greedy().build())
        .handler(this::message)
        .build()
    );
  }

  private void message(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser)context.getSender();

    final String message = context.<String>getRequired("message").replace("</pre>", "");
    String format = this.carbonChat.translations().roleplayFormat();

    if (!user.hasPermission("carbonchat.me.formatting")) {
      format = format.replace("<message>", "<pre><message></pre>");
    }

    final Component component = this.carbonChat.messageProcessor().processMessage(format,
      "displayname", user.displayName(), "message", message);

    if (user.shadowMuted()) {
      user.sendMessage(component);
    } else {
      for (final PlayerUser onlineUser : this.carbonChat.userService().onlineUsers()) {
        if (onlineUser.ignoringUser(user)) {
          continue;
        }

        onlineUser.sendMessage(component);
      }
    }
  }

}
