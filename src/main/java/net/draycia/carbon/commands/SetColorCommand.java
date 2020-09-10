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

import java.util.LinkedHashMap;

public class SetColorCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SetColorCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.isEnabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
      LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
      arguments.put("channel", new LiteralArgument(channel.getKey()));
      arguments.put("color", CarbonUtils.textColorArgument());

      new CommandAPICommand(commandSettings.getName())
        .withArguments(arguments)
        .withAliases(commandSettings.getAliasesArray())
        .withPermission(CommandPermission.fromString("carbonchat.setcolor"))
        .executesPlayer((player, args) -> {
          TextColor color = (TextColor) args[0];
          ChatUser user = carbonChat.getUserService().wrap(player);

          if (!player.hasPermission("carbonchat.setcolor." + channel.getKey())) {
            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
              carbonChat.getLanguage().getString("cannot-set-color"),
              "br", "\n", "input", color.asHexString(),
              "channel", channel.getName()));

            return;
          }

          UserChannelSettings settings = user.getChannelSettings(channel);

          settings.setColor(color);

          user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
            carbonChat.getLanguage().getString("channel-color-set"),
            "br", "\n", "color", "<color:" + color.asHexString() + ">", "channel",
            channel.getName(), "hex", color.asHexString()));
        })
        .register();
    }
  }

}
