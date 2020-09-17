package net.draycia.carbon.managers;

import net.draycia.carbon.common.commands.ChannelCommand;
import net.draycia.carbon.common.commands.ChannelListCommand;
import net.draycia.carbon.common.commands.ChatReloadCommand;
import net.draycia.carbon.common.commands.ClearChatCommand;
import net.draycia.carbon.common.commands.IgnoreCommand;
import net.draycia.carbon.common.commands.MeCommand;
import net.draycia.carbon.common.commands.MessageCommand;
import net.draycia.carbon.common.commands.MuteCommand;
import net.draycia.carbon.common.commands.NicknameCommand;
import net.draycia.carbon.common.commands.ReplyCommand;
import net.draycia.carbon.common.commands.SetColorCommand;
import net.draycia.carbon.common.commands.SpyChannelCommand;
import net.draycia.carbon.common.commands.SudoChannelCommand;
import net.draycia.carbon.common.commands.ToggleCommand;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.CarbonChatBukkit;
import net.draycia.carbon.common.commands.ShadowMuteCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandManager {

  @NonNull
  private final CarbonChatBukkit carbonChat;

  public CommandManager(@NonNull final CarbonChatBukkit carbonChat) {
    this.carbonChat = carbonChat;

    this.reloadCommands();
  }

  private void reloadCommands() {
    new ChannelCommand(this.carbonChat, this.commandSettings("channel"));
    new ChannelListCommand(this.carbonChat, this.commandSettings("channellist"));
    new ChatReloadCommand(this.carbonChat, this.commandSettings("chatreload"));
    new ClearChatCommand(this.carbonChat, this.commandSettings("clearchat"));
    new IgnoreCommand(this.carbonChat, this.commandSettings("ignore"));
    new MeCommand(this.carbonChat, this.commandSettings("me"));
    new MessageCommand(this.carbonChat, this.commandSettings("message"));
    new MuteCommand(this.carbonChat, this.commandSettings("mute"));
    new NicknameCommand(this.carbonChat, this.commandSettings("nickname"));
    new ReplyCommand(this.carbonChat, this.commandSettings("reply"));
    new SetColorCommand(this.carbonChat, this.commandSettings("setcolor"));
    new ShadowMuteCommand(this.carbonChat, this.commandSettings("shadowmute"));
    new SpyChannelCommand(this.carbonChat, this.commandSettings("spy"));
    new SudoChannelCommand(this.carbonChat, this.commandSettings("sudochannel"));
    new ToggleCommand(this.carbonChat, this.commandSettings("toggle"));
  }

  @NonNull
  private CommandSettings commandSettings(@NonNull final String command) {
    return this.carbonChat.commandSettingsRegistry().get(command);
  }

}
