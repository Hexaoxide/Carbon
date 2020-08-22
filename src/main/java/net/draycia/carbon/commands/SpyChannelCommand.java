package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import org.bukkit.entity.Player;

@CommandAlias("spych|spychannel|spy")
@CommandPermission("carbonchat.spy")
public class SpyChannelCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @CommandCompletion("@channel")
    @Syntax("<channel>")
    public void baseCommand(Player player, @Conditions("exists:true") ChatChannel channel) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        UserChannelSettings settings = user.getChannelSettings(channel);

        if (settings.isSpying()) {
            settings.setSpying(false);
            message = carbonChat.getLanguage().getString("spy-toggled-off");
        } else {
            settings.setSpying(true);
            message = carbonChat.getLanguage().getString("spy-toggled-on");
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
                "color", "<color:" + channel.getChannelColor(user).toString() + ">", "channel", channel.getName()));
    }

    @Subcommand("whispers")
    public void whispersCommand(Player player) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        if (user.isSpyingWhispers()) {
            user.setSpyingWhispers(false);
            message = carbonChat.getLanguage().getString("spy-whispers-off");
        } else {
            user.setSpyingWhispers(true);
            message = carbonChat.getLanguage().getString("spy-whispers-on");
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
    }

}
