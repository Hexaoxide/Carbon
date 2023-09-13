/*
 * CarbonChat
 *
 * Copyright (c) 2023 Josua Parks (Vicarious)
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
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
    private volatile Map<Locale, Properties> locales = Map.of();
    private final Path pluginJar;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    private CarbonMessageSource(
        final CarbonEventHandler events,
        final @DataDirectory Path dataDirectory,
        final ConfigManager configManager,
        final Logger logger
    ) throws IOException {
        this.dataDirectory = dataDirectory;
        this.pluginJar = pluginJar();
        this.logger = logger;

        this.defaultLocale = configManager.primaryConfig().defaultLocale();

        this.reloadTranslations();

        events.subscribe(CarbonReloadEvent.class, -99, true, event -> {
            this.reloadTranslations();
        });
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

    private void reloadTranslations() throws IOException {
        final Map<Locale, Properties> map = new HashMap<>();

        final Path localeDirectory = this.dataDirectory.resolve("locale");

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
                final String localeString = localeString(localeFile);
                final @Nullable Locale locale = parseLocale(localeString);

                if (locale == null) {
                    this.logger.warn("Unknown locale '{}'?", localeString);
                    return;
                }

                this.tryLoadLocale(map, localeDirectory, localeFile, locale);
            }));

        try (final Stream<Path> paths = Files.list(localeDirectory)) {
            paths.filter(Files::isRegularFile).forEach(localeFile -> {
                final String localeString = localeString(localeFile);
                final @Nullable Locale locale = parseLocale(localeString);

                if (locale == null) {
                    this.logger.warn("Unknown locale '{}'?", localeString);
                    return;
                }

                if (map.containsKey(locale)) {
                    return;
                }

                this.tryLoadLocale(map, localeDirectory, localeFile, locale);
            });
        }

        this.logger.info("Loaded {} locales: [{}]", map.size(), map.keySet().stream().map(Locale::toString).collect(Collectors.joining(", ")));
        this.locales = Map.copyOf(map);
    }

    private void tryLoadLocale(final Map<Locale, Properties> map, final Path localeDirectory, final Path localeFile, final Locale locale) {
        final @Nullable Properties properties = this.readLocale(localeDirectory, localeFile, locale);
        if (properties != null) {
            map.put(locale, properties);
        }
    }

    private @Nullable Properties readLocale(final Path localeDirectory, final Path localeFile, final Locale locale) {
        this.logger.debug("Found locale {} ({}) in: {}", locale.getDisplayName(), locale, localeFile);

        final Properties properties = new Properties() {
            @Override
            public synchronized Set<Map.Entry<Object, Object>> entrySet() {
                return Collections.unmodifiableSet(
                    (Set<? extends Map.Entry<Object, Object>>) super.entrySet()
                        .stream()
                        .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        };

        try {
            this.loadProperties(properties, localeDirectory, localeFile);

            this.logger.debug("Successfully loaded locale {} ({})", locale.getDisplayName(), locale);
            return properties;
        } catch (final IOException ex) {
            this.logger.warn("Unable to load locale {} ({}) from source: {}", locale.getDisplayName(), locale, localeFile, ex);
            return null;
        }
    }

    @Override
    public String messageOf(final Audience receiver, final String messageKey) {
        Audience audience = receiver;

        if (audience instanceof SourcedAudience sourced) {
            audience = sourced.recipient();
        }

        // Unwrap PlayerCommanders
        if (audience instanceof PlayerCommander playerCommander) {
            audience = playerCommander.carbonPlayer();
        }

        if (audience instanceof CarbonPlayer player) {
            return this.forPlayer(messageKey, player);
        } else {
            return this.fromDefaultLocale(messageKey);
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

        return this.fromDefaultLocale(key);
    }

    private String fromDefaultLocale(final String key) {
        final Properties defaultProperties = this.locales.get(this.defaultLocale);

        if (defaultProperties != null) {
            final String value = defaultProperties.getProperty(key);

            if (value == null) {
                this.logger.warn("No message mapping for key " + key + " in default locale " + this.defaultLocale.getDisplayName());
                return key;
            }

            return value;
        }

        return key;
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
        final Path sourceFile
    ) throws IOException {
        final Path userFile = localeDirectory.resolve(sourceFile.getFileName().toString());
        final boolean samePath = sourceFile.normalize().toAbsolutePath().equals(userFile.normalize().toAbsolutePath());

        if (Files.isRegularFile(userFile)) {
            // If the file in the localeDirectory exists, read it to the properties
            final InputStream inputStream = Files.newInputStream(userFile);
            try (final Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } else if (samePath && !Files.isRegularFile(userFile)) {
            throw new IllegalStateException("sourceFile == userFile, and is not a regular file (%s)".formatted(userFile));
        }

        boolean write = false;

        // Read the file in the jar and add missing entries
        if (Files.isRegularFile(sourceFile) && !samePath) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(sourceFile), StandardCharsets.UTF_8)) {
                final Properties packaged = new Properties();
                packaged.load(reader);

                for (final Map.Entry<Object, Object> entry : packaged.entrySet()) {
                    write |= properties.putIfAbsent(entry.getKey(), entry.getValue()) == null;
                }
            }
        }

        // todo: copy missing entries from default english locale as well?

        // Write properties back to file
        if (write) {
            try (final Writer outputStream = Files.newBufferedWriter(userFile)) {
                properties.store(outputStream, null);
            }
        }
    }

    private static String localeString(final Path localeFile) {
        return localeFile.getFileName().toString().substring("messages-".length()).replace(".properties", "");
    }

    private static @Nullable Locale parseLocale(String localeString) {
        // MC uses no_NO when the player selects nb_NO...
        localeString = localeString.replace("nb_NO", "no_NO");

        return Translator.parseLocale(localeString);
    }

}
