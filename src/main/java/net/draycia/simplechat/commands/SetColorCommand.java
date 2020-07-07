package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@CommandAlias("setcolor")
@CommandPermission("simplechat.setcolor")
public class SetColorCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public SetColorCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    @CommandCompletion("@chatchannel")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, String color) {
        // TODO: condition for ChatChannel existing
        if (channel == null || color == null) {
            throw new ConditionFailedException("Channel or Color not supplied!");
        }

        ChatUser user = simpleChat.getUserService().wrap(player);

        // TODO: move into conditions
        if (!player.hasPermission("simplechat.setcolor." + channel.getName())) {
            throw new ConditionFailedException("You can't set colors for this channel!");
        }

        UserChannelSettings settings = user.getChannelSettings(channel);

        settings.setColor(TextColor.fromHexString(color));

        user.sendMessage(MiniMessage.get().parse(simpleChat.getConfig().getString("language.channel-color-set"),
                "color", "<color:" + color + ">", "channel", channel.getName(), "hex", color));
    }

}
