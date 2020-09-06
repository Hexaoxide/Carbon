package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;

public class MuteCommand {

    private final CarbonChat carbonChat;

    public MuteCommand(CarbonChat carbonChat, CommandSettings commandSettings) {
        this.carbonChat = carbonChat;

        if (!commandSettings.isEnabled()) {
            return;
        }

        CommandUtils.handleDuplicateCommands(commandSettings);

        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("player", CarbonUtils.chatUserArgument());

        new CommandAPICommand(commandSettings.getName())
                .withArguments(arguments)
                .withAliases(commandSettings.getAliasesArray())
                .withPermission(CommandPermission.fromString("carbonchat.mute"))
                .executes(this::execute)
                .register();
    }

    private void execute(CommandSender sender, Object[] args) {
        ChatUser user = (ChatUser) args[0];
        Audience audience = carbonChat.getAdventureManager().getAudiences().audience(sender);

        if (user.isShadowMuted()) {
            user.setMuted(false);
            String format = carbonChat.getLanguage().getString("no-longer-muted");

            Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
                    "player", user.asOfflinePlayer().getName());

            audience.sendMessage(message);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Permission permission = carbonChat.getPermission();
                String format;

                if (permission.playerHas(null, user.asOfflinePlayer(), "carbonchat.mute.exempt")) {
                    format = carbonChat.getLanguage().getString("mute-exempt");
                } else {
                    user.setMuted(true);
                    format = carbonChat.getLanguage().getString("is-now-muted");
                }

                Component message = carbonChat.getAdventureManager().processMessage(format, "br", "\n",
                        "player", user.asOfflinePlayer().getName());

                audience.sendMessage(message);
            });
        }
    }
}
