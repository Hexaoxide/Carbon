package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class AliasedChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  @NonNull
  private final ChatChannel chatChannel;

  @NonNull
  private final String commandName;

  public AliasedChannelCommand(@NonNull final CarbonChat carbonChat, @NonNull final ChatChannel chatChannel) {
    this.carbonChat = carbonChat;
    this.chatChannel = chatChannel;

    this.commandName = chatChannel.key();

    final LinkedHashMap<String, Argument> setChannelArguments = new LinkedHashMap<>();

    new CommandAPICommand(this.commandName)
      .withArguments(setChannelArguments)
      .withPermission(CommandPermission.fromString("carbonchat.channel"))
      .executesPlayer(this::channel)
      .register();

    final LinkedHashMap<String, Argument> sendMessageArguments = new LinkedHashMap<>();
    sendMessageArguments.put("message", new GreedyStringArgument());

    new CommandAPICommand(this.commandName)
      .withArguments(sendMessageArguments)
      .withPermission(CommandPermission.fromString("carbonchat.channel.message"))
      .executesPlayer(this::sendMessage)
      .register();
  }

  private void channel(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);

    if (user.channelSettings(this.chatChannel()).ignored()) {
      user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, this.chatChannel().cannotUseMessage(),
        "br", "\n",
        "color", "<" + this.chatChannel().channelColor(user).toString() + ">",
        "channel", this.chatChannel().name()));

      return;
    }

    user.selectedChannel(this.chatChannel());

    user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, this.chatChannel().switchMessage(),
      "br", "\n",
      "color", "<" + this.chatChannel().channelColor(user).toString() + ">",
      "channel", this.chatChannel().name()));
  }

  private void sendMessage(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);
    final String message = (String) args[0];

    final Component component = this.chatChannel().sendMessage(user, message, false);

    this.carbonChat.adventureManager().audiences().console().sendMessage(component);
  }

  @NonNull
  public ChatChannel chatChannel() {
    return this.chatChannel;
  }

  @NonNull
  public String commandName() {
    return this.commandName;
  }
}
