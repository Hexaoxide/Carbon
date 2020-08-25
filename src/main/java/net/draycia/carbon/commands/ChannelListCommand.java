package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@CommandAlias("chlist|channellist")
@CommandPermission("carbonchat.channellist")
public class ChannelListCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @Syntax("[player]")
    public void baseCommand(Player player) {
        Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();
        ChatUser user = carbonChat.getUserService().wrap(player);
        getListAndSend(player, user, allChannels);
    }

        @CommandPermission("carbonchat.channellist.others")
        @Subcommand("user")
        @CommandCompletion("@players")
        @Syntax("<player>")
        public void baseCommand(Player player, ChatUser user) {
            ChatUser cmdSender = carbonChat.getUserService().wrap(player);
            if (!user.isOnline()) {
                String mustBeOnline = carbonChat.getLanguage().getString("user-must-be-online");
                cmdSender.sendMessage(carbonChat.getAdventureManager().processMessage(mustBeOnline, "br", "\n", "player", user.asOfflinePlayer().getName()));
                return;
            }
            Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();
            getListAndSend(player, user, allChannels);
        }

    private void getListAndSend(Player player, ChatUser user, Iterator<ChatChannel> allChannels) {
        ChatChannel channel;
        List<ChatChannel> canSee = new ArrayList<>();
        List<ChatChannel> cannotSee = new ArrayList<>();

        while (allChannels.hasNext()) {
            channel = allChannels.next();

            if (channel.canPlayerSee(user, true)) {
                canSee.add(channel);
            } else {
                cannotSee.add(channel);
            }
        }

        Iterator<ChatChannel> visibleChannels = canSee.iterator();
        TextComponent.Builder availableList = TextComponent.builder("");

        makeList(visibleChannels, availableList);

        String availableFormat = carbonChat.getLanguage().getString("available-channels-list");
        Component availableComponent = carbonChat.getAdventureManager().processMessage(availableFormat, "br", "\n");
        availableComponent = ((TextComponent)availableComponent).replaceFirst(Pattern.compile(Pattern.quote("<list>")), (ac) ->  availableList);

        carbonChat.getUserService().wrap(player).sendMessage(availableComponent);

        if (player.hasPermission("carbonchat.channellist.bypass") && !cannotSee.isEmpty()) {

            Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();
            TextComponent.Builder unavailableList = TextComponent.builder("");

            makeList(invisibleChannels, unavailableList);

            String unavailableFormat = carbonChat.getLanguage().getString("unavailable-channels-list");
            Component unavailableComponent = carbonChat.getAdventureManager().processMessage(unavailableFormat, "br", "\n");
            unavailableComponent = ((TextComponent)unavailableComponent).replaceFirst(Pattern.compile(Pattern.quote("<list>")), (uac) ->  unavailableList);

            carbonChat.getUserService().wrap(player).sendMessage(unavailableComponent);
        }
    }

    private void makeList(Iterator<ChatChannel> iterator, TextComponent.Builder list) {
        String listSeparator = carbonChat.getLanguage().getString("channel-list-separator");
        while (iterator.hasNext()) {
            ChatChannel channel = iterator.next();
            list.append(TextComponent.of(channel.getName()));

            if (iterator.hasNext()) {
                if (listSeparator != null) {
                    list.append(TextComponent.of(listSeparator));
                } else {
                    list.append(TextComponent.of(", "));
                }
            }
        }
    }
}

