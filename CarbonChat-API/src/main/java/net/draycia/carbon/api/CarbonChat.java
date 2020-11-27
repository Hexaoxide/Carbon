package net.draycia.carbon.api;

import cloud.commandframework.CommandManager;
import net.draycia.carbon.api.config.CarbonTranslations;
import net.draycia.carbon.api.adventure.MessageProcessor;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.commands.settings.CommandSettingsRegistry;
import net.draycia.carbon.api.config.CarbonSettings;
import net.draycia.carbon.api.config.ChannelSettings;
import net.draycia.carbon.api.config.ModerationSettings;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.api.users.UserService;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public interface CarbonChat {

  LegacyComponentSerializer LEGACY =
    LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .character(LegacyComponentSerializer.SECTION_CHAR)
      .useUnusualXRepeatedCharacterHexFormat()
      .build();

  void reloadConfig();

  @NonNull File dataDirectory();

  @NonNull UUID resolveUUID(@NonNull String name);

  @NonNull String resolveName(@NonNull UUID uuid);

  @NonNull Logger logger();

  @NonNull Path dataFolder();

  @NonNull <T extends PlayerUser> UserService<T> userService();

  @NonNull MessageProcessor messageProcessor();

  @NonNull MessageService messageService();

  @NonNull CarbonSettings carbonSettings();

  @NonNull ChannelSettings channelSettings();

  @NonNull ChannelRegistry channelRegistry();

  @NonNull CommandSettingsRegistry commandSettings();

  @NonNull CarbonTranslations translations();

  @NonNull ModerationSettings moderationSettings();

  @NonNull GsonComponentSerializer gsonSerializer();

  @NonNull CommandManager<CarbonUser> commandManager();

}
