package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.StaticArgument;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserChannelSettings;
import net.draycia.carbon.common.utils.TextColorArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SetChannelColorCommand {

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("methodref.receiver.bound.invalid")
  public SetChannelColorCommand(final @NonNull CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("setchannelcolor");

    if (commandSettings == null || !commandSettings.enabled()) {
      return;
    }

    for (final ChatChannel channel : this.carbonChat.channelRegistry()) {
      commandManager.command(
        commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
          commandManager.createDefaultCommandMeta())
          .senderType(PlayerUser.class) // player
          .permission("carbonchat.setchannelcolor." + channel.key())
          .argument(StaticArgument.of(channel.key()))
          .argument(TextColorArgument.of("color")) // remember to replace this when cloud 1.1.0 releases
          .handler(context -> {
            final PlayerUser user = (PlayerUser) context.getSender();
            final TextColor color = context.get("color");
            final UserChannelSettings settings = user.channelSettings(channel);

            settings.color(color);

            user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(
              this.carbonChat.translations().channelColorSet(),
              "color", "<color:" + color.asHexString() + ">", "channel",
              channel.name(), "hex", color.asHexString()));
          }).build()
      );
    }
  }

}
