package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@CommandAlias("spych|spychannel|spy")
public class SpyChannelCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public SpyChannelCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

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

        user.sendMessage(MiniMessage.get().parse(message, "br", "\n",
                "color", "<color:" + channel.getColor().toString() + ">", "channel", channel.getName()));
    }

}
