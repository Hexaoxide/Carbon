package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@CommandAlias("tch|togglec|togglechannel|toggle")
@CommandPermission("simplechat.toggle")
public class ToggleCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ToggleCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        String message;

        if (user.ignoringChannel(channel)) {
            user.setIgnoringChannel(channel, true);
            message = channel.getToggleOffMessage();
        } else {
            user.setIgnoringChannel(channel, false);
            message = channel.getToggleOnMessage();
        }

        user.asAudience().sendMessage(MiniMessage.instance().parse(message,
                "color", channel.getColor().toString(), "channel", channel.getName()));
    }

}
