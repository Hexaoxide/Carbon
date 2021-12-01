/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.translation.Translator;
import net.kyori.moonshine.message.IMessageSource;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonMessageSource implements IMessageSource<Audience, String> {

    private final Locale defaultLocale;
    private final Map<Locale, Properties> locales = new HashMap<>();
    private final Path pluginJar;
    private final Logger logger;

    @Inject
    private CarbonMessageSource(
        final @ForCarbon Path dataDirectory,
        final ConfigFactory configFactory,
        final Logger logger
    ) throws IOException {
        this.pluginJar = pluginJar();
        this.logger = logger;

        this.defaultLocale = Objects.requireNonNull(configFactory.primaryConfig()).defaultLocale();

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
                // MC uses no_NO when the player selects nb_NO...
                final @Nullable Locale locale = Translator.parseLocale(localeString.replace("nb_NO", "no_NO"));

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

    private static @NonNull Path pluginJar() {
        try {
            URL sourceUrl = CarbonMessageSource.class.getProtectionDomain().getCodeSource().getLocation();
            // Some class loaders give the full url to the class, some give the URL to its jar.
            // We want the containing jar, so we will unwrap jar-schema code sources.
            if (sourceUrl.getProtocol().equals("jar")) {
                final int exclamationIdx = sourceUrl.getPath().lastIndexOf('!');
                if (exclamationIdx != -1) {
                    sourceUrl = new URL(sourceUrl.getPath().substring(0, exclamationIdx));
                }
            }
            return Paths.get(sourceUrl.toURI());
        } catch (final URISyntaxException | MalformedURLException ex) {
            throw new RuntimeException("Could not locate plugin jar", ex);
        }
    }

    @Override
    public String messageOf(final Audience receiver, final String messageKey) {
        Audience audience = receiver;

        // Unwrap PlayerCommanders
        if (audience instanceof PlayerCommander playerCommander) {
            audience = playerCommander.carbonPlayer();
        }

        if (audience instanceof CarbonPlayer player) {
            return this.forPlayer(messageKey, player);
        } else {
            return this.forAudience(messageKey, audience);
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
                user.accept(stream.map(path -> path.relativize(this.pluginJar)));
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
