package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("%channelName")
@CommandPermission("carbonchat.switch")
public class AliasedChannelCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    private final ChatChannel chatChannel;

    public AliasedChannelCommand(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }

    @Default
    @Syntax("[message]")
    public void baseCommand(Player player, @Optional String[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (!chatChannel.canPlayerUse(user)) {
            throw new ConditionFailedException(chatChannel.getCannotUseMessage());
        }

        if (args == null || args.length == 0) {
            user.setSelectedChannel(getChatChannel());

            user.sendMessage(carbonChat.getAdventureManager().processMessage(getChatChannel().getSwitchMessage(),
                    "color", "<" + getChatChannel().getChannelColor(user).toString() + ">",
                    "channel", getChatChannel().getName()));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Component component = getChatChannel().sendMessage(user, String.join(" ", args), false);

                carbonChat.getLogger().info(LegacyComponentSerializer.legacySection().serialize(component)
                        .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
            });
        }
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }
}
