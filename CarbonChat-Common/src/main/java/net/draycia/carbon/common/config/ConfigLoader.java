package net.draycia.carbon.common.config;

import net.draycia.carbon.api.CarbonChatProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigLoader<L extends AbstractConfigurationLoader<CommentedConfigurationNode>> {

  private final @NonNull Class<L> format;

  public ConfigLoader(final @NonNull Class<L> format) {
    this.format = format;
  }

  public <B extends AbstractConfigurationLoader.Builder<?, L>> CommentedConfigurationNode loadConfig(final @NonNull String file) throws ConfigurateException {
    final B builder;

    if (this.format == HoconConfigurationLoader.class) {
      builder = (B) HoconConfigurationLoader.builder().prettyPrinting(true);
    } else {
      builder = (B) YamlConfigurationLoader.builder().nodeStyle(NodeStyle.BLOCK);
    }

    final File config = this.findAndCopyFile(this.fileInDataDir(file));
    final L loader = this.loadConfigFile(builder, config);
    final CommentedConfigurationNode node = loader.load();

    if (!config.exists()) {
      loader.save(node);
    }

    return node;
  }

  private @NonNull File fileInDataDir(final @NonNull String file) {
    return new File(CarbonChatProvider.carbonChat().dataFolder().toFile(), file);
  }

  private <B extends AbstractConfigurationLoader.Builder<?, L>> L loadConfigFile(final B builder, final @NonNull File config) {
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

  private @NonNull File findAndCopyFile(final @NonNull File file) {
    if (file.exists()) {
      return file;
    }

    final String resource = "/" + file.getName();
    final InputStream inputStream = this.getClass().getResourceAsStream(resource);

    if (inputStream == null) {
      return file;
    }

    return this.copyFileToDataDir(inputStream, file);
  }

  public @NonNull File copyFileToDataDir(final @NonNull InputStream source, final @NonNull File file) {
    try {
      Files.copy(source, file.toPath());
    } catch (final IOException ignored) {
    }

    return file;
  }

}
