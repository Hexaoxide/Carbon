package net.draycia.carbon.api;

import net.draycia.carbon.api.adventure.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.CommandSettingsRegistry;
import net.draycia.carbon.api.users.UserService;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CarbonChat {

  @NonNull UserService userService();

  @NonNull MessageProcessor messageProcessor();

  @NonNull ChannelRegistry channelRegistry();

  @NonNull CommandSettingsRegistry commandSettingsRegistry();

  @NonNull CarbonTranslations translations();

}
