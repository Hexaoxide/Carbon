package net.draycia.carbon.api;

import net.draycia.carbon.api.adventure.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.users.UserService;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

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

  @NonNull Path dataFolder();

  @NonNull UserService userService();

  @NonNull MessageProcessor messageProcessor();

  @NonNull ChannelRegistry channelRegistry();

  @NonNull CommandSettingsRegistry commandSettingsRegistry();

  @NonNull CarbonTranslations translations();

  @NonNull ModerationSettings moderationSettings();

}
