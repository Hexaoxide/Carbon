package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class SpyChannelCommand {

    private final CarbonChat carbonChat;

    public SpyChannelCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.spy.name", "spy");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.spy.aliases");

        LinkedHashMap<String, Argument> channelArguments = new LinkedHashMap<>();
        channelArguments.put("channel", CarbonUtils.channelArgument());

        new CommandAPICommand(commandName)
                .withArguments(channelArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.spy"))
                .executesPlayer(this::execute)
                .register();

        LinkedHashMap<String, Argument> whisperArguments = new LinkedHashMap<>();
        whisperArguments.put("channel", new LiteralArgument("whispers"));

        new CommandAPICommand(commandName)
                .withArguments(whisperArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.spy"))
                .executesPlayer(this::executeWhispers)
                .register();

        LinkedHashMap<String, Argument> everythingArguments = new LinkedHashMap<>();
        everythingArguments.put("channel", new LiteralArgument("*"));
        everythingArguments.put("should-spy", new BooleanArgument());

        new CommandAPICommand(commandName)
                .withArguments(everythingArguments)
                .withAliases(commandAliases.toArray(new String[0]))
                .withPermission(CommandPermission.fromString("carbonchat.spy"))
                .executesPlayer(this::executeEverything) // lul
                .register();
    }

    private void execute(Player player, Object[] args) {
        ChatChannel chatChannel = (ChatChannel) args[0];
        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        UserChannelSettings settings = user.getChannelSettings(chatChannel);

        if (settings.isSpying()) {
            settings.setSpying(false);
            message = carbonChat.getLanguage().getString("spy-toggled-off");
        } else {
            settings.setSpying(true);
            message = carbonChat.getLanguage().getString("spy-toggled-on");
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n",
                "color", "<color:" + chatChannel.getChannelColor(user).toString() + ">", "channel", chatChannel.getName()));
    }

    private void executeWhispers(Player player, Object[] args) {
        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        if (user.isSpyingWhispers()) {
            user.setSpyingWhispers(false);
            message = carbonChat.getLanguage().getString("spy-whispers-off");
        } else {
            user.setSpyingWhispers(true);
            message = carbonChat.getLanguage().getString("spy-whispers-on");
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
    }

    private void executeEverything(Player player, Object[] args) {
        Boolean shouldSpy = (Boolean) args[0];

        ChatUser user = carbonChat.getUserService().wrap(player);

        String message;

        if (shouldSpy) {
            user.setSpyingWhispers(true);

            for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
                user.getChannelSettings(channel).setSpying(true);
            }

            message = carbonChat.getLanguage().getString("spy-everything-off");
        } else {
            user.setSpyingWhispers(false);

            for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
                user.getChannelSettings(channel).setSpying(false);
            }

            message = carbonChat.getLanguage().getString("spy-everything-on");
        }

        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player, message, "br", "\n"));
    }

}
