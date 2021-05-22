package net.draycia.carbon.common.config;

import java.io.File;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

@DefaultQualifier(NonNull.class)
public class ConfigLoader {

    public CommentedConfigurationNode loadConfig(final File file) throws ConfigurateException {
        final var builder = HoconConfigurationLoader.builder().prettyPrinting(true);

        final var loader = this.loadConfigFile(builder, file);
        final CommentedConfigurationNode node = loader.load();

        if (!file.exists()) {
            loader.save(node);
        }

        return node;
    }

    private HoconConfigurationLoader loadConfigFile(final HoconConfigurationLoader.Builder builder, final File config) {
        return builder
            .defaultOptions(opts -> {
                final ConfigurateComponentSerializer serializer = ConfigurateComponentSerializer.configurate();

                return opts.shouldCopyDefaults(true).serializers(serializerBuilder ->
                    serializerBuilder.registerAll(serializer.serializers()));
            })
            .file(config)
            .build();
    }

}
