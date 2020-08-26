package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.kyori.adventure.text.format.TextColor;

import java.util.LinkedHashMap;
import java.util.List;

public class SetColorCommand {

    private final CarbonChat carbonChat;

    public SetColorCommand(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String commandName = carbonChat.getConfig().getString("commands.setcolor.name", "setcolor");
        List<String> commandAliases = carbonChat.getConfig().getStringList("commands.setcolor.aliases");

        for (ChatChannel channel : carbonChat.getChannelManager().getRegistry().values()) {
            LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
            arguments.put("channel", new LiteralArgument(channel.getKey()));
            arguments.put("color", new StringArgument());

            new CommandAPICommand(commandName)
                    .withArguments(arguments)
                    .withAliases(commandAliases.toArray(new String[0]))
                    .withPermission(CommandPermission.fromString("carbonchat.setcolor"))
                    .executesPlayer((player, args) -> {
                        String colorInput = (String) args[1];

                        ChatUser user = carbonChat.getUserService().wrap(player);

                        if (!player.hasPermission("carbonchat.setcolor." + channel.getKey())) {
                            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                                    carbonChat.getLanguage().getString("cannot-set-color"),
                                    "br", "\n", "input", colorInput,
                                    "channel", channel.getName()));

                            return;
                        }

                        UserChannelSettings settings = user.getChannelSettings(channel);
                        TextColor color = CarbonUtils.parseColor(user, colorInput);

                        if (color == null) {
                            user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                                    carbonChat.getLanguage().getString("invalid-color"),
                                    "br", "\n", "input", colorInput,
                                    "channel", channel.getName()));

                            return;
                        }

                        settings.setColor(color);

                        user.sendMessage(carbonChat.getAdventureManager().processMessageWithPapi(player,
                                carbonChat.getLanguage().getString("channel-color-set"),
                                "br", "\n", "color", "<color:" + color.asHexString() + ">", "channel",
                                channel.getName(), "hex", color.asHexString()));
                    })
                    .register();
        }
    }

}
