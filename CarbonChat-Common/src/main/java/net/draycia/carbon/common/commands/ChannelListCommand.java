package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ChannelListCommand {

  private @NonNull final CarbonChat carbonChat;

  public ChannelListCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("channellist");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(CarbonUser.class) // console & player
        .permission("carbonchat.channellist")
        .argument(PlayerUserArgument.optionalPlayerUserArgument()) // carbonchat.channellist.other
        .handler(context -> {
          if (context.getOptional("user").isPresent()) {
            this.channelListOther(context);
          } else {
            this.channelListSelf(context);
          }
        })
        .build()
    );
  }

  public void channelListSelf(@NonNull final CommandContext<CarbonUser> context) {
    this.listAndSend(context.getSender(), (PlayerUser) context.getSender(), this.carbonChat.channelRegistry().iterator());
  }

  public void channelListOther(@NonNull final CommandContext<CarbonUser> context) {
    final CarbonUser sender = context.getSender();
    final PlayerUser user = context.get("user");

    if (!user.online()) {
      final String mustBeOnline = this.carbonChat.translations().userMustBeOnline();

      sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor()
        .processMessage(mustBeOnline, "player", user.name()));

      return;
    }

    final Iterator<ChatChannel> allChannels = this.carbonChat.channelRegistry().iterator();
    this.listAndSend(sender, user, allChannels);
  }

  private void listAndSend(@NonNull final CarbonUser sender, @NonNull final PlayerUser user,
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

    final String availableFormat = this.carbonChat.translations().availableChannelsList();
    Component availableComponent = this.carbonChat.messageProcessor().processMessage(availableFormat, "br", "\n");
    availableComponent = availableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), ac -> availableList);

    sender.sendMessage(Identity.nil(), availableComponent);

    if (sender.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {
      final Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
      final TextComponent.Builder unavailableList = Component.text();

      this.makeList(invisibleChannels, unavailableList);

      final String unavailableFormat = this.carbonChat.translations().unavailableChannelsList();
      Component unavailableComponent = this.carbonChat.messageProcessor().processMessage(unavailableFormat, "br", "\n");
      unavailableComponent = unavailableComponent.replaceFirstText(Pattern.compile(Pattern.quote("<list>")), uac -> unavailableList);

      sender.sendMessage(Identity.nil(), unavailableComponent);
    }
  }

  private void makeList(@NonNull final Iterator<@NonNull ChatChannel> iterator, final TextComponent.@NonNull Builder list) {
    final String listSeparator = this.carbonChat.translations().channelListSeparator();
    final Component listSeparatorComponent = Component.text(listSeparator);

    while (iterator.hasNext()) {
      list.append(Component.text(iterator.next().name()));

      if (iterator.hasNext()) {
        list.append(listSeparatorComponent);
      }
    }
  }
}
