package net.draycia.carbon.common.commands.misc;

import com.intellectualsites.commands.CommandManager;
import net.draycia.carbon.api.users.ChatUser;
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
import net.draycia.carbon.common.commands.ShadowMuteCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandRegistrar {

  private CommandRegistrar() {

  }

  public static void registerCommands(@NonNull final CommandManager<ChatUser> commandManager) {
    new ChannelCommand(commandManager);
    new ChannelListCommand(commandManager);
    new ChatReloadCommand(commandManager);
    new ClearChatCommand(commandManager);
    new IgnoreCommand(commandManager);
    new MeCommand(commandManager);
    new MessageCommand(commandManager);
    new MuteCommand(commandManager);
    new NicknameCommand(commandManager);
    new ReplyCommand(commandManager);
    new SetColorCommand(commandManager);
    new ShadowMuteCommand(commandManager);
    new SpyChannelCommand(commandManager);
    new SudoChannelCommand(commandManager);
    new ToggleCommand(commandManager);
  }

}
