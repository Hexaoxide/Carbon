package net.draycia.carbon.common.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.moonshine.message.IMessageSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.CarbonJar;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.config.PrimaryConfig;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.translation.Translator;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonMessageSource implements IMessageSource<String, Audience> {

    private final Locale defaultLocale;
    private final Map<Locale, Properties> locales = new HashMap<>();
    private final Path pluginJar;
    private final Logger logger;

    @Inject
    private CarbonMessageSource(
        final @ForCarbon Path dataDirectory,
        final PrimaryConfig primaryConfig,
        final @CarbonJar Path pluginJar,
        final Logger logger
    ) throws IOException {
        this.pluginJar = pluginJar;
        this.logger = logger;

        final @Nullable Locale configLocale = Translator.parseLocale(primaryConfig.defaultLocale());

        if (configLocale != null) {
            this.defaultLocale = configLocale;
        } else {
            this.defaultLocale = Locale.ENGLISH;
        }

        final Path localeDirectory = dataDirectory.resolve("locale");

        // Create locale directory
        if (!Files.exists(localeDirectory)) {
            Files.createDirectories(localeDirectory);
        }

        this.walkPluginJar(stream -> stream.filter(Files::isRegularFile)
            .filter(it -> {
                final String pathString = it.toString();
                return pathString.startsWith("/locale/messages-")
                    && pathString.endsWith(".properties");
            })
            .forEach(localeFile -> {
                final String localeString = localeFile.getFileName().toString().substring("messages-".length()).replace(".properties", "");
                final @Nullable Locale locale = Translator.parseLocale(localeString);

                if (locale == null) {
                    this.logger.warn("Unknown locale '{}'?", localeString);
                    return;
                }

                this.logger.info("Found locale {} ({}) in: {}", locale.getDisplayName(), locale, localeFile); // todo - debug

                final Properties properties = new Properties();

                try {
                    this.loadProperties(properties, localeDirectory, localeFile);
                    this.locales.put(locale, properties);

                    this.logger.info("Successfully loaded locale {} ({})", locale.getDisplayName(), locale);
                } catch (final IOException ex) {
                    this.logger.warn("Unable to load locale {} ({}) from source: {}", locale.getDisplayName(), locale, localeFile, ex);
                }
            }));
    }

    @Override
    public String message(final String key, final Audience receiver) {
        if (receiver instanceof CarbonPlayer player) {
            return this.forPlayer(key, player);
        } else {
            return this.forAudience(key, receiver);
        }
    }

    private String forPlayer(final String key, final CarbonPlayer player) {
        if (player.locale() != null) {
            final var properties = this.locales.get(player.locale());

            if (properties != null) {
                final var message = properties.getProperty(key);

                if (message != null) {
                    return message;
                }
            }
        }

        return this.forAudience(key, player);
    }

    private String forAudience(final String key, final Audience audience) {
        final String value = this.locales.get(this.defaultLocale).getProperty(key);

        if (value == null) {
            throw new IllegalStateException("No message mapping for key " + key);
        }

        return value;
    }

    private void walkPluginJar(final Consumer<Stream<Path>> user) throws IOException {
        if (Files.isDirectory(this.pluginJar)) {
            try (final var stream = Files.walk(this.pluginJar)) {
                user.accept(stream);
            }
            return;
        }
        try (final FileSystem jar = FileSystems.newFileSystem(this.pluginJar, this.getClass().getClassLoader())) {
            final Path root = jar.getRootDirectories()
                .iterator()
                .next();
            try (final var stream = Files.walk(root)) {
                user.accept(stream);
            }
        }
    }

    private void loadProperties(
        final Properties properties,
        final Path localeDirectory,
        final Path localeFile
    ) throws IOException {
        final Path savedFile = localeDirectory.resolve(localeFile.getFileName().toString());

        // If the file in the localeDirectory exists, read it to the properties
        if (Files.isRegularFile(savedFile)) {
            final InputStream inputStream = Files.newInputStream(savedFile);
            try (final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }

        boolean write = !Files.isRegularFile(savedFile);

        // Read the file in the jar and add missing entries
        try (final InputStream stream = Files.newInputStream(localeFile)) {
            final Properties packaged = new Properties();
            packaged.load(stream);

            for (final Map.Entry<Object, Object> entry : packaged.entrySet()) {
                write |= properties.putIfAbsent(entry.getKey(), entry.getValue()) == null;
            }
        }

        // Write properties back to file
        if (write) {
            try (final Writer outputStream = Files.newBufferedWriter(savedFile)) {
                properties.store(outputStream, null);
            }
        }
    }

}
