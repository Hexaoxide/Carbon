package net.draycia.simplechat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("me|rp")
@CommandPermission("simplechat.me")
public class MeCommand extends BaseCommand {

    private SimpleChat simpleChat;

    public MeCommand(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;
    }

    @Default
    public void baseCommand(Player player, String... args) {
        String message = String.join(" ", args);
        String format = PlaceholderAPI.setPlaceholders(player, simpleChat.getConfig().getString("language.me"));
        Component component = MiniMessage.instance().parse(format, "message", message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            simpleChat.getAudiences().player(onlinePlayer).sendMessage(component);
        }
    }

}
