package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

public class MeCommand {

    private final CarbonChat carbonChat;

    public MeCommand(CarbonChat carbonChat, CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
        channelArguments.put("message", new GreedyStringArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(channelArguments)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.me"))
                .executesPlayer(this::execute)
                .register();
    }

    private void execute(Player player, Object[] args) {
        String message = ((String) args[0]).replace("</pre>", "");
        String format = PlaceholderAPI.setPlaceholders(player, carbonChat.getLanguage().getString("me"));

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
