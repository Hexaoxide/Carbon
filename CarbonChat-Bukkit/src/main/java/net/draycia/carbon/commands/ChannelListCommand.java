package net.draycia.carbon.commands;

import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.commands.CommandSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ChannelListCommand {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public ChannelListCommand(@NonNull final CarbonChatBukkit carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channellist"))
      .executesPlayer(this::executeSelf)
      .register();

    final LinkedHashMap<String, Argument> argumentsOther = new LinkedHashMap<>();
    argumentsOther.put("player", CarbonUtils.onlineChatUserArgument());

    new CommandAPICommand(commandSettings.name())
      .withArguments(argumentsOther)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channellist.other"))
      .executes(this::executeOther)
      .register();
  }

  public void executeSelf(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final Iterator<ChatChannel> allChannels = this.carbonChat.channelRegistry().iterator();
    final ChatUser user = this.carbonChat.userService().wrap(player.getUniqueId());

    this.listAndSend(player, user, allChannels);
  }

  public void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    final ChatUser user = (ChatUser) args[0];

    final OfflinePlayer player = Bukkit.getOfflinePlayer(user.uuid());

    if (!player.isOnline()) {
      final String mustBeOnline = this.carbonChat.translations().userMustBeOnline();
      cmdSender.sendMessage(this.carbonChat.messageProcessor().processMessage(mustBeOnline, "br", "\n", "player", player.getName()));
      return;
    }

    final Iterator<ChatChannel> allChannels = this.carbonChat.channelRegistry().iterator();
    this.listAndSend(sender, user, allChannels);
  }

  private void listAndSend(@NonNull final CommandSender sender, @NonNull final ChatUser user,
                           @NonNull final Iterator<ChatChannel> allChannels) {
    ChatChannel channel;
    final List<ChatChannel> canSee = new ArrayList<>();
    final List<ChatChannel> cannotSee = new ArrayList<>();

    while (allChannels.hasNext()) {
      channel = allChannels.next();

      if (channel.canPlayerSee(user, true)) {
        canSee.add(channel);
      } else {
        cannotSee.add(channel);
      }
    }

    final Iterator<ChatChannel> visibleChannels = canSee.iterator();
    final TextComponent.Builder availableList = TextComponent.builder("");

    this.makeList(visibleChannels, availableList);

    final String availableFormat = this.carbonChat.translations().availableChannelsList();
    Component availableComponent = this.carbonChat.messageProcessor().processMessage(availableFormat, "br", "\n");
    availableComponent = availableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), ac -> availableList);

    final Audience cmdSender;

    if (sender instanceof Player) {
      cmdSender = this.carbonChat.userService().wrap(((Player) sender).getUniqueId());
    } else {
      cmdSender = this.carbonChat.messageProcessor().audiences().console();
    }

    cmdSender.sendMessage(availableComponent);

    if (sender.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {
      final Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
      final TextComponent.Builder unavailableList = TextComponent.builder("");

      this.makeList(invisibleChannels, unavailableList);

      final String unavailableFormat = this.carbonChat.translations().unavailableChannelsList();
      Component unavailableComponent = this.carbonChat.messageProcessor().processMessage(unavailableFormat, "br", "\n");
      unavailableComponent = unavailableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), uac -> unavailableList);

      cmdSender.sendMessage(unavailableComponent);
    }
  }

  private void makeList(@NonNull final Iterator<@NonNull ChatChannel> iterator, final TextComponent.@NonNull Builder list) {
    final String listSeparator = this.carbonChat.translations().channelListSeparator();
    // TODO: Larry, why did you double assign the listSeparatorComponent?
    final Component listSeparatorComponent = TextComponent.of(listSeparator);

    while (iterator.hasNext()) {
      list.append(TextComponent.of(iterator.next().name()));

      if (iterator.hasNext()) {
        list.append(listSeparatorComponent);
      }
    }
  }
}
