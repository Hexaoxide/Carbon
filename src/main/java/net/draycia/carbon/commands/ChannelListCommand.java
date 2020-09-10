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
import java.util.LinkedHashMap;
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
    final Iterator<ChatChannel> allChannels = this.carbonChat.getChannelManager().registry().values().iterator();
    final ChatUser user = this.carbonChat.getUserService().wrap(player);

    this.listAndSend(player, user, allChannels);
  }

  public void executeOther(@NonNull final CommandSender sender, @NonNull final Object @NonNull [] args) {
    final Audience cmdSender = this.carbonChat.getAdventureManager().audiences().audience(sender);
    final ChatUser user = (ChatUser) args[0];

    if (!user.online()) {
      final String mustBeOnline = this.carbonChat.getLanguage().getString("user-must-be-online");
      cmdSender.sendMessage(this.carbonChat.getAdventureManager().processMessage(mustBeOnline, "br", "\n", "player", user.offlinePlayer().getName()));
      return;
    }

    final Iterator<ChatChannel> allChannels = this.carbonChat.getChannelManager().registry().values().iterator();
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

    final String availableFormat = this.carbonChat.getLanguage().getString("available-channels-list");
    Component availableComponent = this.carbonChat.getAdventureManager().processMessage(availableFormat, "br", "\n");
    availableComponent = availableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), ac -> availableList);

    final Audience audience = this.carbonChat.getAdventureManager().audiences().audience(sender);

    audience.sendMessage(availableComponent);

    if (sender.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {
      final Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
      final TextComponent.Builder unavailableList = TextComponent.builder("");

      this.makeList(invisibleChannels, unavailableList);

      final String unavailableFormat = this.carbonChat.getLanguage().getString("unavailable-channels-list");
      Component unavailableComponent = this.carbonChat.getAdventureManager().processMessage(unavailableFormat, "br", "\n");
      unavailableComponent = unavailableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), uac -> unavailableList);

      audience.sendMessage(unavailableComponent);
    }
  }

  private void makeList(@NonNull final Iterator<@NonNull ChatChannel> iterator, final TextComponent.@NonNull Builder list) {
    final String listSeparator = this.carbonChat.getLanguage().getString("channel-list-separator", ", ");
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
