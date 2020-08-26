package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@CommandAlias("setcolor|setcolour")
@CommandPermission("carbonchat.setcolor")
public class SetColorCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@chatchannel @nothing")
    @Syntax("<channel> <color>")
    public void baseCommand(Player player, @Conditions("canuse:true") ChatChannel channel, String input) {
        if (channel == null || input == null) {
            throw new ConditionFailedException("Channel or Color not supplied!");
        }

        ChatUser user = carbonChat.getUserService().wrap(player);

        if (!player.hasPermission("carbonchat.setcolor." + channel.getKey())) {
            throw new ConditionFailedException("You can't set colors for this channel!");
        }

        UserChannelSettings settings = user.getChannelSettings(channel);
        TextColor color = CarbonUtils.parseColor(user, input);

        settings.setColor(color);

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, carbonChat.getLanguage().getString("channel-color-set"),
                "br", "\n", "color", "<color:" + color.asHexString() + ">", "channel", channel.getName(), "hex", color.asHexString()));
    }

}
