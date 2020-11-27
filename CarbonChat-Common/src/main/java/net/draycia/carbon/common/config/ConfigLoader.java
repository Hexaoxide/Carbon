package net.draycia.carbon.common.config;

import net.draycia.carbon.api.CarbonChatProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigLoader<L extends AbstractConfigurationLoader<CommentedConfigurationNode>> {

  private final @NonNull Class<L> format;

  public ConfigLoader(final @NonNull Class<L> format) {
    this.format = format;
  }

  public <B extends AbstractConfigurationLoader.Builder<?, L>> CommentedConfigurationNode loadAndSaveNode(final @NonNull File config, final boolean save) throws ConfigurateException {
    final B builder;

    if (this.format == HoconConfigurationLoader.class) {
      builder = (B) HoconConfigurationLoader.builder();
    } else {
      builder = (B) YamlConfigurationLoader.builder();
    }

    final L loader = this.loadConfigFile(builder, config, save);
    final CommentedConfigurationNode node = loader.load();

    loader.save(node);

    return node;
  }

  public @NonNull CommentedConfigurationNode loadAndSaveNode(final @NonNull String config, final boolean save) throws ConfigurateException {
    final File dataDirFile = this.fileInDataDir(config);

    if (dataDirFile.exists()) {
      return this.loadAndSaveNode(dataDirFile, save);
    }

    final String file;

    if (config.startsWith("/")) {
      file = config;
    } else {
      file = "/" + config;
    }

    final InputStream inputStream = this.getClass().getResourceAsStream(file);

    if (inputStream == null) {
      throw new IllegalStateException("File not found! [" + file + "]");
    }

    this.copyFileToDataDir(inputStream);

    return this.loadAndSaveNode(dataDirFile, save);
  }

  public @NonNull File fileInDataDir(final @NonNull String file) {
    CarbonChatProvider.carbonChat().logger().info("Loading file " + file);
    return new File(CarbonChatProvider.carbonChat().dataFolder().toFile(), file);
  }

  public boolean copyFileToDataDir(final @NonNull InputStream source) {
    try {
      Files.copy(source, CarbonChatProvider.carbonChat().dataFolder(), StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException ex) {
      return false;
    }

    return true;
  }

  private <B extends AbstractConfigurationLoader.Builder<?, L>> L loadConfigFile(final B builder, final @NonNull File config, final boolean save) {
    // TODO: save file from jar if it doesn't exist

    return builder
      .defaultOptions(opts -> {
        return opts.shouldCopyDefaults(true).serializers(serializerBuilder -> {
          serializerBuilder.register(Key.class, KeySerializer.INSTANCE)
            .register(Sound.class, SoundSerializer.INSTANCE);
        });
      })
      .file(config)
      .build();
  }

}
