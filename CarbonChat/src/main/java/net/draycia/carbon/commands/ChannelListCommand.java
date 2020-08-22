package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;

@CommandAlias("chlist|channellist")
@CommandPermission("carbonchat.channellist")
public class ChannelListCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @Syntax("[player]")
    public void baseCommand(Player player) {
        TextComponent.Builder component = TextComponent.builder("");
        Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();
        ChatUser user = carbonChat.getUserService().wrap(player);

        getChannelLists(player, user, allChannels, component);
        user.sendMessage(component.build());
    }

        @CommandPermission("carbonchat.channellist.others")
        @Subcommand("user")
        @CommandCompletion("@players")
        @Syntax("<player>")
        public void baseCommand(Player player, ChatUser user) {
            ChatUser cmdSender = carbonChat.getUserService().wrap(player);
            TextComponent.Builder component = TextComponent.builder("");
            Iterator<ChatChannel> allChannels = carbonChat.getChannelManager().getRegistry().values().iterator();

            getChannelLists(player, user, allChannels, component);

            cmdSender.sendMessage(component.build());
        }

    private void getChannelLists(CommandSender player, ChatUser user, Iterator<ChatChannel> allChannels, TextComponent.Builder component) {
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

        component.append(TextComponent.of("Available Channels: ").color(NamedTextColor.GREEN));
        makeList(visibleChannels, component);
        component.append("\n").build();

        if (player.hasPermission("carbonchat.channellist.bypass")) {
            Iterator<ChatChannel> invisibleChannels = cannotSee.iterator();

            component.append(TextComponent.of("Unavailable Channels: ").color(NamedTextColor.RED));
            makeList(invisibleChannels, component);
        }
    }


    private void makeList(Iterator<ChatChannel> iterator, TextComponent.Builder component) {

        while (iterator.hasNext()) {
            ChatChannel chatChannel = iterator.next();

            component.append(TextComponent.of(chatChannel.getName()));

            if (iterator.hasNext()) {
                component.append(TextComponent.of(", "));
            } else {
                component.append(TextComponent.of(". "));
            }
        }
    }
}

