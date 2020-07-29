package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("%channelName")
@CommandPermission("carbonchat.switch")
public class AliasedChannelCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    private final ChatChannel chatChannel;

    public AliasedChannelCommand(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    @Default
    public void baseCommand(Player player, @Optional String[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (!chatChannel.canPlayerUse(user)) {
            throw new ConditionFailedException(chatChannel.getCannotUseMessage());
        }

        if (args == null || args.length == 0) {
            user.setSelectedChannel(getChatChannel());

            user.sendMessage(carbonChat.getAdventureManager().processMessage(getChatChannel().getSwitchMessage(),
                    "color", "<" + getChatChannel().getColor().toString() + ">",
                    "channel", getChatChannel().getName()));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                getChatChannel().sendMessage(user, String.join(" ", args), false);
            });
        }
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }
}
