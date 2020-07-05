package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("%channelName")
@CommandPermission("simplechat.switch")
public class AliasedChannelCommand extends BaseCommand {

    private SimpleChat simpleChat;
    private ChatChannel chatChannel;

    public AliasedChannelCommand(SimpleChat simpleChat, ChatChannel chatChannel) {
        this.simpleChat = simpleChat;
        this.chatChannel = chatChannel;
    }

    @Default
    public void baseCommand(Player player, @Optional String[] args) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        if (!chatChannel.canPlayerUse(user)) {
            // TODO: send message
            return;
        }

        if (args == null || args.length == 0) {
            user.setSelectedChannel(getChatChannel());

            user.sendMessage(MiniMessage.get().parse(
                    getChatChannel().getSwitchMessage(), "color", "<" + getChatChannel().getColor().toString() + ">",
                    "channel", getChatChannel().getName()));
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                getChatChannel().sendMessage(user, String.join(" ", args), false);
            });
        }
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }
}
