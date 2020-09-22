package net.draycia.carbon.api;

import net.draycia.carbon.api.adventure.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserService;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public interface CarbonChat {

  LegacyComponentSerializer LEGACY =
    LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .character('§')
      .useUnusualXRepeatedCharacterHexFormat()
      .build();

  void reloadConfig();

  @NonNull Logger logger();

  @NonNull Path dataFolder();

  @NonNull <T extends ChatUser> UserService<T> userService();

  @NonNull MessageProcessor messageProcessor();

  @NonNull MessageService messageService();

  @NonNull ChannelRegistry channelRegistry();

  @NonNull CommandSettingsRegistry commandSettingsRegistry();

  @NonNull CarbonTranslations translations();

  @NonNull ModerationSettings moderationSettings();

}
