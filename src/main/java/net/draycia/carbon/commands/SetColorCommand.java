package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@CommandAlias("setcolor")
@CommandPermission("carbonchat.setcolor")
public class SetColorCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@chatchannel")
    @Syntax("<channel> <color>")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, String color) {
        if (channel == null || color == null) {
            throw new ConditionFailedException("Channel or Color not supplied!");
        }

        ChatUser user = carbonChat.getUserService().wrap(player);

        if (!player.hasPermission("carbonchat.setcolor." + channel.getKey())) {
            throw new ConditionFailedException("You can't set colors for this channel!");
        }

        UserChannelSettings settings = user.getChannelSettings(channel);

        settings.setColor(TextColor.fromHexString(color));

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, carbonChat.getLanguage().getString("channel-color-set"),
                "br", "\n", "color", "<color:" + color + ">", "channel", channel.getName(), "hex", color));
    }

}
