package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import org.bukkit.entity.Player;

@CommandAlias("tch|togglec|togglechannel|toggle")
public class ToggleCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ToggleCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel) {
        String message;

        if (simpleChat.togglePlayerChannelMute(player, channel)) {
            message = channel.getToggleOnMessage();
        } else {
            message = channel.getToggleOffMessage();
        }

        message = MiniMessageParser.handlePlaceholders(message, "color", channel.getColor().toString());

        simpleChat.getPlatform().player(player).sendMessage(MiniMessageParser.parseFormat(message, "channel", channel.getName()));
    }

}
