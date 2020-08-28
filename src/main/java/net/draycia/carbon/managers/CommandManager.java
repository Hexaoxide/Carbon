package net.draycia.carbon.managers;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.commands.*;

public class CommandManager {

    public CommandManager(CarbonChat carbonChat) {
        reloadCommands(carbonChat);
    }

    public void reloadCommands(CarbonChat carbonChat) {
        new ChannelCommand(carbonChat);
        new ChannelListCommand(carbonChat);
        new ChatReloadCommand(carbonChat);
        new ClearChatCommand(carbonChat);
        new IgnoreCommand(carbonChat);
        new MeCommand(carbonChat);
        new MessageCommand(carbonChat);
        new MuteCommand(carbonChat);
        new NicknameCommand(carbonChat);
        new ReplyCommand(carbonChat);
        new SetColorCommand(carbonChat);
        new ShadowMuteCommand(carbonChat);
        new SpyChannelCommand(carbonChat);
        new ToggleCommand(carbonChat);
    }

}
