package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SetColorCommand {

  private @NonNull final CarbonChat carbonChat;

  public SetColorCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("setcolor");

    if (!commandSettings.enabled()) {
      return;
    }

    // TODO: Test and fix
    for (final ChatChannel channel : this.carbonChat.channelRegistry()) {
      commandManager.command(
        commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
          commandManager.createDefaultCommandMeta())
          .withSenderType(PlayerUser.class) // player
          .withPermission("carbonchat.setcolor")
          // TODO: colorArgument
          //.argument(CommandUtils.colorArgument())
          .handler(context -> {
            final PlayerUser user = (PlayerUser) context.getSender();
            final TextColor color = context.getRequired("color");

            if (!user.hasPermission("carbonchat.setcolor." + channel.key())) {
              user.sendMessage(this.carbonChat.messageProcessor().processMessage(
                this.carbonChat.translations().cannotSetColor(), "input", color.asHexString(),
                "channel", channel.name()));

              return;
            }

            final UserChannelSettings settings = user.channelSettings(channel);

            settings.color(color);

            user.sendMessage(this.carbonChat.messageProcessor().processMessage(
              this.carbonChat.translations().channelColorSet(),
              "color", "<color:" + color.asHexString() + ">", "channel",
              channel.name(), "hex", color.asHexString()));
          }).build()
      );
    }
  }

}
