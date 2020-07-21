package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("%channelName")
@CommandPermission("simplechat.switch")
public class AliasedChannelCommand extends BaseCommand {

    private SimpleChat simpleChat;
    private ChatChannel chatChannel;

    private final ChatChannel chatChannel;

    public AliasedChannelCommand(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    @Default
    public void baseCommand(Player player, @Optional String[] args) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        if (!chatChannel.canPlayerUse(user)) {
            throw new ConditionFailedException(chatChannel.getCannotUseMessage());
        }

        if (args == null || args.length == 0) {
            user.setSelectedChannel(getChatChannel());

            user.sendMessage(simpleChat.getAdventureManager().processMessage(getChatChannel().getSwitchMessage(),
                    "color", "<" + getChatChannel().getColor().toString() + ">",
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
