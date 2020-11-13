package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ChannelListCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ChannelListCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final List<Argument> channelArguments = new ArrayList<>();

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channellist"))
      .executesPlayer(this::executeSelf)
      .register();

    final List<Argument> argumentsOther = new ArrayList<>();
    argumentsOther.add(CarbonUtils.onlineChatUserArgument("player"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(argumentsOther)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.channellist.other"))
      .executes(this::executeOther)
      .register();
  }

  public void executeSelf(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final Iterator<ChatChannel> allChannels = this.carbonChat.channelManager().registry().values().iterator();
    final ChatUser user = this.carbonChat.userService().wrap(player);

    this.listAndSend(player, user, allChannels);
  }

  public void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience cmdSender = this.carbonChat.adventureManager().audiences().sender(sender);
    final ChatUser user = (ChatUser) args[0];

    if (!user.online()) {
      final String mustBeOnline = this.carbonChat.language().getString("user-must-be-online");
      cmdSender.sendMessage(this.carbonChat.adventureManager().processMessage(mustBeOnline, "br", "\n", "player", user.offlinePlayer().getName()));
      return;
    }

    final Iterator<ChatChannel> allChannels = this.carbonChat.channelManager().registry().values().iterator();
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
    final TextComponent.Builder availableList = Component.text();

    this.makeList(visibleChannels, availableList);

    final String availableFormat = this.carbonChat.language().getString("available-channels-list");
    Component availableComponent = this.carbonChat.adventureManager().processMessage(availableFormat, "br", "\n");
    availableComponent = availableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), ac -> availableList);

    final Audience audience = this.carbonChat.adventureManager().audiences().sender(sender);

    audience.sendMessage(availableComponent);

    if (sender.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {
      final Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
      final TextComponent.Builder unavailableList = Component.text();

      this.makeList(invisibleChannels, unavailableList);

      final String unavailableFormat = this.carbonChat.language().getString("unavailable-channels-list");
      Component unavailableComponent = this.carbonChat.adventureManager().processMessage(unavailableFormat, "br", "\n");
      unavailableComponent = unavailableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), uac -> unavailableList);

      audience.sendMessage(unavailableComponent);
    }
  }

  private void makeList(@NonNull final Iterator<@NonNull ChatChannel> iterator, final TextComponent.@NonNull Builder list) {
    final String listSeparator = this.carbonChat.language().getString("channel-list-separator", ", ");
    // TODO: Larry, why did you double assign the listSeparatorComponent?
    final Component listSeparatorComponent = Component.text(listSeparator);

    while (iterator.hasNext()) {
      list.append(Component.text(iterator.next().name()));

      if (iterator.hasNext()) {
        list.append(listSeparatorComponent);
      }
    }
  }
}
