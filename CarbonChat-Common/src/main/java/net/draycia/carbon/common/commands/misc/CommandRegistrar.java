package net.draycia.carbon.common.commands.misc;

import cloud.commandframework.CommandManager;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.common.commands.AliasedChannelCommand;
import net.draycia.carbon.common.commands.ChannelCommand;
import net.draycia.carbon.common.commands.ChannelListCommand;
import net.draycia.carbon.common.commands.ChatReloadCommand;
import net.draycia.carbon.common.commands.ClearChatCommand;
import net.draycia.carbon.common.commands.IgnoreCommand;
import net.draycia.carbon.common.commands.MeCommand;
import net.draycia.carbon.common.commands.MessageCommand;
import net.draycia.carbon.common.commands.MuteCommand;
import net.draycia.carbon.common.commands.NicknameCommand;
import net.draycia.carbon.common.commands.PingOptionsCommand;
import net.draycia.carbon.common.commands.ReplyCommand;
import net.draycia.carbon.common.commands.SetChannelColorCommand;
import net.draycia.carbon.common.commands.SpyChannelCommand;
import net.draycia.carbon.common.commands.SudoChannelCommand;
import net.draycia.carbon.common.commands.ToggleCommand;
import net.draycia.carbon.common.commands.ShadowMuteCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandRegistrar {

  private CommandRegistrar() {

  }

  public static void registerCommands(final @NonNull CommandManager<CarbonUser> commandManager) {
    new ChannelCommand(commandManager);
    new ChannelListCommand(commandManager);
    new ChatReloadCommand(commandManager);
    new ClearChatCommand(commandManager);
    new IgnoreCommand(commandManager);
    new MeCommand(commandManager);
    new MessageCommand(commandManager);
    new MuteCommand(commandManager);
    new NicknameCommand(commandManager);
    new PingOptionsCommand(commandManager);
    new ReplyCommand(commandManager);
    new SetChannelColorCommand(commandManager);
    new ShadowMuteCommand(commandManager);
    new SpyChannelCommand(commandManager);
    new SudoChannelCommand(commandManager);
    new ToggleCommand(commandManager);

    for (final ChatChannel channel : CarbonChatProvider.carbonChat().channelRegistry()) {
      if (channel instanceof TextChannel) {
        new AliasedChannelCommand(commandManager, (TextChannel) channel);
      }
    }
  }

}
