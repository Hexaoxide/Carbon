package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

@CommandAlias("nick|nickname")
@CommandPermission("carbonchat.nickname")
public class NicknameCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    public void baseCommand(Player player, String nickname, @Optional ChatUser target) {
        ChatUser sender = carbonChat.getUserService().wrap(player);

        Component component = carbonChat.getAdventureManager().processMessage(nickname);
        String legacyNickname = LegacyComponentSerializer.legacySection().serialize(component);

        if (target != null) {
            target.setNickname(nickname);

            String message = carbonChat.getConfig().getString("language.other-nickname-set");
            sender.sendMessage(carbonChat.getAdventureManager().processMessage(
                    message, "nickname", nickname));

            target.asPlayer().setDisplayName(legacyNickname);
        } else {
            sender.setNickname(nickname);

            String message = carbonChat.getConfig().getString("language.nickname-set");
            sender.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                    message, "nickname", nickname));

            sender.asPlayer().setDisplayName(legacyNickname);
        }
    }

}
