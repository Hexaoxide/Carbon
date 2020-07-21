package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import org.bukkit.entity.Player;

@CommandAlias("spych|spychannel|spy")
@CommandPermission("simplechat.spy")
public class SpyChannelCommand extends BaseCommand {

    @Dependency
    private SimpleChat simpleChat;

    @Default
    @CommandCompletion("@channel")
    public void baseCommand(Player player, @Conditions("exists:true") ChatChannel channel) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        String message;

        UserChannelSettings settings = user.getChannelSettings(channel);

        if (settings.isSpying()) {
            settings.setSpying(false);
            message = simpleChat.getConfig().getString("language.spy-toggled-off");
        } else {
            settings.setSpying(true);
            message = simpleChat.getConfig().getString("language.spy-toggled-on");
        }

        user.sendMessage(simpleChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));
    }

    @Subcommand("whispers")
    public void whispersCommand(Player player) {
        ChatUser user = simpleChat.getUserService().wrap(player);

        String message;

        if (user.isSpyingWhispers()) {
            user.setSpyingWhispers(false);
            message = simpleChat.getConfig().getString("language.spy-whispers-off");
        } else {
            user.setSpyingWhispers(true);
            message = simpleChat.getConfig().getString("language.spy-whispers-on");
        }

        user.sendMessage(simpleChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
    }

}
