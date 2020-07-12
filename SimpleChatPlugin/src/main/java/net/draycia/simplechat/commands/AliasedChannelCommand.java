package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.impls.AllianceChatChannel;
import net.draycia.simplechat.channels.impls.NationChatChannel;
import net.draycia.simplechat.channels.impls.PartyChatChannel;
import net.draycia.simplechat.channels.impls.TownChatChannel;
import net.draycia.simplechat.storage.ChatUser;
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
            if (chatChannel.isTownChat() && !((TownChatChannel)chatChannel).isInTown(user)) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.town-cannot-use"));
            } else if (chatChannel.isNationChat() && !((NationChatChannel)chatChannel).isInNation(user)) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("language.nation-cannot-use"));
            } else if (chatChannel.isAllianceChat() && !((AllianceChatChannel)chatChannel).isInNation(user)) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("alliance-cannot-use"));
            } else if (chatChannel.isPartyChat() && !((PartyChatChannel)chatChannel).isInParty(user)) {
                throw new ConditionFailedException(simpleChat.getConfig().getString("party-cannot-use"));
            } else {
                throw new ConditionFailedException(simpleChat.getConfig().getString("cannot-use-channel"));
            }
        }

        if (args == null || args.length == 0) {
            user.setSelectedChannel(getChatChannel());

            user.sendMessage(simpleChat.processMessage(getChatChannel().getSwitchMessage(),
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
