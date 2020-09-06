package net.draycia.carbon.managers;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.commands.*;
import net.draycia.carbon.storage.CommandSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandManager {

    private final @NonNull CarbonChat carbonChat;

    public CommandManager(@NonNull CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
        reloadCommands(carbonChat);
    }

    private void reloadCommands(@NonNull CarbonChat carbonChat) {
        new ChannelCommand(carbonChat, getCommandSettings("channel"));
        new ChannelListCommand(carbonChat, getCommandSettings("channellist"));
        new ChatReloadCommand(carbonChat, getCommandSettings("chatreload"));
        new ClearChatCommand(carbonChat, getCommandSettings("clearchat"));
        new IgnoreCommand(carbonChat, getCommandSettings("ignore"));
        new MeCommand(carbonChat, getCommandSettings("me"));
        new MessageCommand(carbonChat, getCommandSettings("message"));
        new MuteCommand(carbonChat, getCommandSettings("mute"));
        new NicknameCommand(carbonChat, getCommandSettings("nickname"));
        new ReplyCommand(carbonChat, getCommandSettings("reply"));
        new SetColorCommand(carbonChat, getCommandSettings("setcolor"));
        new ShadowMuteCommand(carbonChat, getCommandSettings("shadowmute"));
        new SpyChannelCommand(carbonChat, getCommandSettings("spy"));
        new SudoChannelCommand(carbonChat, getCommandSettings("sudochannel"));
        new ToggleCommand(carbonChat, getCommandSettings("toggle"));
    }

    private @NonNull CommandSettings getCommandSettings(@NonNull String command) {
        ConfigurationSection section = carbonChat.getCommandsConfig().getConfigurationSection(command);

        return new CommandSettings(section.getBoolean("enabled"), section.getString("name"),
                section.getStringList("aliases"));
    }

}
