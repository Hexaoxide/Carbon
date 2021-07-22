package net.draycia.carbon.common.config;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.serialisation.gson.LocaleSerializerConfigurate;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

@DefaultQualifier(NonNull.class)
public class ConfigLoader {

    private final Path dataDirectory;
    private final LocaleSerializerConfigurate locale;

    @Inject
    public ConfigLoader(
        @ForCarbon final Path dataDirectory,
        final LocaleSerializerConfigurate locale
    ) {
        this.dataDirectory = dataDirectory;
        this.locale = locale;
    }

    public ConfigurationLoader<?> configurationLoader(final Path file) {
        return HoconConfigurationLoader.builder()
            .prettyPrinting(true)
            .defaultOptions(opts -> {
                final ConfigurateComponentSerializer serializer =
                    ConfigurateComponentSerializer.configurate();

                return opts.shouldCopyDefaults(true).serializers(serializerBuilder ->
                    serializerBuilder.registerAll(serializer.serializers())
                        .register(Locale.class, this.locale)
                );
            })
            .path(file)
            .build();
    }

    public <T> @Nullable T load(final Class<T> clazz, final String fileName) throws IOException {
        if (!Files.exists(this.dataDirectory)) {
            Files.createDirectories(this.dataDirectory);
        }

        final Path file = this.dataDirectory.resolve(fileName);

        final var loader = this.configurationLoader(file);

        try {
            final var node = loader.load();
            final @Nullable T config = node.get(clazz);

            if (!Files.exists(file)) {
                node.set(clazz, config);
                loader.save(node);
            }

            return config;
        } catch (final ConfigurateException exception) {
            exception.printStackTrace();
            return null;
        }
    }

}
