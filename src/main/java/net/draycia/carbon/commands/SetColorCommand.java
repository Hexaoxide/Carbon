package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SetColorCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SetColorCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    for (final ChatChannel channel : this.carbonChat.channelManager().registry().values()) {
      final List<Argument> arguments = new ArrayList<>();
      arguments.add(new LiteralArgument(channel.key()));
      arguments.add(CarbonUtils.textColorArgument("color"));

      new CommandAPICommand(commandSettings.name())
        .withArguments(arguments)
        .withAliases(commandSettings.aliases())
        .withPermission(CommandPermission.fromString("carbonchat.setcolor"))
        .executesPlayer((player, args) -> {
          final TextColor color = (TextColor) args[0];
          final ChatUser user = this.carbonChat.userService().wrap(player);

          if (!player.hasPermission("carbonchat.setcolor." + channel.key())) {
            user.sendMessage(carbonChat.adventureManager().processMessageWithPapi(player,
              this.carbonChat.language().getString("cannot-set-color"),
              "br", "\n", "input", color.asHexString(),
              "channel", channel.name()));

            return;
          }

          final UserChannelSettings settings = user.channelSettings(channel);

          settings.color(color);

          user.sendMessage(carbonChat.adventureManager().processMessageWithPapi(player,
            this.carbonChat.language().getString("channel-color-set"),
            "br", "\n", "color", "<color:" + color.asHexString() + ">", "channel",
            channel.name(), "hex", color.asHexString()));
        })
        .register();
    }
  }

}
