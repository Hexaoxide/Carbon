package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@CommandAlias("chcolor|channelcolor")
@CommandPermission("simplechat.setcolor")
public class ChannelColorCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ChannelColorCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel @chatcolors")
    public void baseCommand(Player player, ChatChannel chatChannel, String color) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        user.getChannelSettings(chatChannel).setColor(TextColor.fromHexString(color));
    }

}
