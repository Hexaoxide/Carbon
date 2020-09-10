package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;

public class SpyChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SpyChannelCommand(@NonNull CarbonChat carbonChat, @NonNull CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
    channelArguments.put("channel", CarbonUtils.channelArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::execute)
      .register();

    LinkedHashMap<String, Argument> whisperArguments = new LinkedHashMap<>();
    whisperArguments.put("channel", new LiteralArgument("whispers"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(whisperArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::executeWhispers)
      .register();

    LinkedHashMap<String, Argument> everythingArguments = new LinkedHashMap<>();
    everythingArguments.put("channel", new LiteralArgument("*"));
    everythingArguments.put("should-spy", new BooleanArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(everythingArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::executeEverything) // lul
      .register();
  }

  private void execute(@NonNull Player player, @NonNull Object @NonNull [] args) {
    ChatChannel chatChannel = (ChatChannel) args[0];
    ChatUser user = carbonChat.getUserService().wrap(player);

    String message;

    UserChannelSettings settings = user.getChannelSettings(chatChannel);

    if (settings.spying()) {
      settings.spying(false);
      message = carbonChat.getLanguage().getString("spy-toggled-off");
    } else {
      settings.spying(true);
      message = carbonChat.getLanguage().getString("spy-toggled-on");
    }

    user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
      "color", "<color:" + chatChannel.getChannelColor(user).toString() + ">", "channel", chatChannel.getName()));
  }

  private void executeWhispers(@NonNull Player player, @NonNull Object @NonNull [] args) {
    ChatUser user = carbonChat.getUserService().wrap(player);

    String message;

    if (user.isSpyingWhispers()) {
      user.setSpyingWhispers(false);
      message = carbonChat.getLanguage().getString("spy-whispers-off");
    } else {
      user.setSpyingWhispers(true);
      message = carbonChat.getLanguage().getString("spy-whispers-on");
    }

    user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
  }

  private void executeEverything(@NonNull Player player, @NonNull Object @NonNull [] args) {
    Boolean shouldSpy = (Boolean) args[0];

    ChatUser user = carbonChat.getUserService().wrap(player);

    String message;

    if (shouldSpy) {
      user.setSpyingWhispers(true);

      for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
        user.getChannelSettings(channel).spying(true);
      }

      message = carbonChat.getLanguage().getString("spy-everything-off");
    } else {
      user.setSpyingWhispers(false);

      for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
        user.getChannelSettings(channel).spying(false);
      }

      message = carbonChat.getLanguage().getString("spy-everything-on");
    }

    user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
  }

}
