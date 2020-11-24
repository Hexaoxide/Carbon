package net.draycia.carbon.common.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

import java.io.File;

public class ConfigLoader<L extends AbstractConfigurationLoader<CommentedConfigurationNode>> {

  public <B extends AbstractConfigurationLoader.Builder<?, L>> CommentedConfigurationNode loadAndSaveNode(final @NonNull B builder,
                                                     final @NonNull File config,
                                                     final boolean save) throws ConfigurateException {
    final L loader = this.loadConfigFile(builder, config, save);
    final CommentedConfigurationNode node = loader.load();

    loader.save(node);

    return node;
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
