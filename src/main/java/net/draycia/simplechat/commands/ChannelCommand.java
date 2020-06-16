package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.kyori.text.adapter.bukkit.TextAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("ch|channel|switch")
@CommandPermission("simplechat.use.switch")
public class ChannelCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public ChannelCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, @Optional String[] args) {
        if (args == null || args.length == 0) {
            simpleChat.setPlayerChannel(player, channel);

            String switchMessage = MiniMessageParser.handlePlaceholders(channel.getSwitchMessage(),
                    "color", "<" + channel.getColor().toString() + ">", "channel", channel.getName());

            TextAdapter.sendMessage(player, MiniMessageParser.parseFormat(switchMessage));
        } else {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(simpleChat, () -> {
                channel.sendMessage(player, String.join(" ", args));
            });
        }
    }

}
