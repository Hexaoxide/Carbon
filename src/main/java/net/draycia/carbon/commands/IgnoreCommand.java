package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

public class IgnoreCommand {

    private final CarbonChat carbonChat;

    public IgnoreCommand(CarbonChat carbonChat, CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        CommandUtils.handleDuplicateCommands(commandSettings);

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
        channelArguments.put("player", CarbonUtils.chatUserArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(channelArguments)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.ignore"))
                .executesPlayer(this::execute)
                .register();
    }

    private void execute(Player player, Object[] args) {
        ChatUser targetUser = (ChatUser) args[0];
        ChatUser user = carbonChat.getUserService().wrap(player);

        if (user.isIgnoringUser(targetUser)) {
            user.setIgnoringUser(targetUser, false);
            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                    carbonChat.getLanguage().getString("not-ignoring-user"),
                    "br", "\n", "player", targetUser.asOfflinePlayer().getName()));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();
                String format;

                if (permission.playerHas(null, targetUser.asOfflinePlayer(), "carbonchat.ignore.exempt")) {
                    format = carbonChat.getLanguage().getString("ignore-exempt");
                } else {
                    user.setIgnoringUser(targetUser, true);
                    format = carbonChat.getLanguage().getString("ignoring-user");
                }

                Component message = carbonChat.getAdventureManager().processMessageWithPapi(player, format,
                        "br", "\n", "sender", player.getDisplayName(), "player",
                        targetUser.asOfflinePlayer().getName());

                user.sendMessage(message);
            });

        }
    }

}
