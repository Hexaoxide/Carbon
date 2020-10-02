package net.draycia.carbon.api;

import net.draycia.carbon.api.config.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.CarbonSettings;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.api.users.UserService;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public interface CarbonChat {

  LegacyComponentSerializer LEGACY =
    LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .character(LegacyComponentSerializer.SECTION_CHAR)
      .useUnusualXRepeatedCharacterHexFormat()
      .build();

  void reloadConfig();

  @NonNull Logger logger();

  @NonNull Path dataFolder();

  @NonNull <T extends ChatUser> UserService<T> userService();

  @NonNull MessageProcessor messageProcessor();

  @NonNull MessageService messageService();

  @NonNull CarbonSettings carbonSettings();

  @NonNull ChannelSettings channelSettings();

  @NonNull ChannelRegistry channelRegistry();

  @NonNull CommandSettingsRegistry commandSettings();

  @NonNull CarbonTranslations translations();

  @NonNull ModerationSettings moderationSettings();

  @NonNull GsonComponentSerializer gsonSerializer();

}
