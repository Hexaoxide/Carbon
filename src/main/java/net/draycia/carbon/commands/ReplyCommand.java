package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class ReplyCommand {

    private final CarbonChat carbonChat;

    public ReplyCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("message", new GreedyStringArgument());

        String commandName = carbonChat.getConfig().getString("commands.reply.name", "reply");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.reply.aliases");

        new CommandAPICommand(commandName)
                .withArguments(arguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.reply"))
                .executesPlayer(this::execute)
                .register();
    }

    private void execute(Player player, Object[] args) {
        String input = (String) args[0];

        ChatUser user = carbonChat.getUserService().wrap(player);

        if (input == null || input.isEmpty()) {
            String message = carbonChat.getLanguage().getString("reply-message-blank");
            Component component = carbonChat.getAdventureManager().processMessage(message, "br", "\n");
            user.sendMessage(component);
            return;
        }

        if (user.getReplyTarget() == null) {
            String message = carbonChat.getLanguage().getString("no-reply-target");
            Component component = carbonChat.getAdventureManager().processMessage(message, "br", "\n");
            user.sendMessage(component);
            return;
        }

        ChatUser targetUser = carbonChat.getUserService().wrap(user.getReplyTarget());

        targetUser.sendMessage(user, input);
    }

}
