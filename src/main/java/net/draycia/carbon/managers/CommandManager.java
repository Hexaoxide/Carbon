package net.draycia.carbon.managers;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.commands.ChannelCommand;
import net.draycia.carbon.commands.ChannelListCommand;
import net.draycia.carbon.commands.ChatReloadCommand;
import net.draycia.carbon.commands.ClearChatCommand;
import net.draycia.carbon.commands.IgnoreCommand;
import net.draycia.carbon.commands.MeCommand;
import net.draycia.carbon.commands.MessageCommand;
import net.draycia.carbon.commands.MuteCommand;
import net.draycia.carbon.commands.NicknameCommand;
import net.draycia.carbon.commands.ReplyCommand;
import net.draycia.carbon.commands.SetColorCommand;
import net.draycia.carbon.commands.ShadowMuteCommand;
import net.draycia.carbon.commands.SpyChannelCommand;
import net.draycia.carbon.commands.SudoChannelCommand;
import net.draycia.carbon.commands.ToggleCommand;
import net.draycia.carbon.storage.CommandSettings;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CommandManager {

  @NonNull
  private final CarbonChat carbonChat;

  public CommandManager(@NonNull final CarbonChat carbonChat) {
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
    final ConfigurationSection section = this.carbonChat.getCommandsConfig().getConfigurationSection(command);

    return new CommandSettings(section.getBoolean("enabled"), section.getString("name"),
      section.getStringList("aliases"));
  }

}
