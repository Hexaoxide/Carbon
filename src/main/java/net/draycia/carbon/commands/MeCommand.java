package net.draycia.carbon.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("me|rp")
@CommandPermission("carbonchat.me")
public class MeCommand extends BaseCommand {

    @Dependency
    private CarbonChat carbonChat;

    @Default
    @Syntax("<message>")
    public void baseCommand(Player player, String... args) {
        if (args == null || args.length == 0) {
            return;
        }

        String message = String.join(" ", args).replace("</pre>", "");
        String format = PlaceholderAPI.setPlaceholders(player, carbonChat.getConfig().getString("language.me"));

        if (!player.hasPermission("carbonchat.me.formatting")) {
            format = format.replace("<message>", "<pre><message></pre>");
        }

        Component component = carbonChat.getAdventureManager().processMessage(format,  "br", "\n",
                "displayname", player.getDisplayName(), "message", message);

        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.isShadowMuted()) {
            user.sendMessage(component);
        } else {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (carbonChat.getUserService().wrap(onlinePlayer).isIgnoringUser(user)) {
                    continue;
                }

                carbonChat.getAdventureManager().getAudiences().player(onlinePlayer).sendMessage(component);
            }
        }
    }

}
