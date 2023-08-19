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
package net.draycia.carbon.common.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import net.draycia.carbon.api.event.CarbonEventHandler;
import net.draycia.carbon.common.DataDirectory;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.event.events.CarbonReloadEvent;
import net.draycia.carbon.common.serialisation.gson.LocaleSerializerConfigurate;
import net.draycia.carbon.common.util.FileUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

@DefaultQualifier(NonNull.class)
@Singleton
public final class ConfigManager {

    private static final String PRIMARY_CONFIG_FILE_NAME = "config.conf";
    private static final String COMMAND_SETTINGS_FILE_NAME = "command-settings.conf";

    private final Path dataDirectory;
    private final LocaleSerializerConfigurate locale;
    private final Logger logger;

    private volatile @MonotonicNonNull PrimaryConfig primaryConfig = null;

    @Inject
    private ConfigManager(
        final CarbonEventHandler events,
        @DataDirectory final Path dataDirectory,
        final LocaleSerializerConfigurate locale,
        final Logger logger
    ) {
        this.dataDirectory = dataDirectory;
        this.locale = locale;
        this.logger = logger;

        events.subscribe(CarbonReloadEvent.class, -100, true, event -> this.reloadPrimaryConfig());
    }

    public void reloadPrimaryConfig() {
        final @Nullable PrimaryConfig load = this.load(PrimaryConfig.class, PRIMARY_CONFIG_FILE_NAME);
        if (load != null) {
            this.primaryConfig = load;
        } else {
            this.logger.error("Failed to reload primary config, see above for further details");
        }
    }

    public PrimaryConfig primaryConfig() {
        if (this.primaryConfig == null) {
            synchronized (this) {
                if (this.primaryConfig == null) {
                    final @Nullable PrimaryConfig load = this.load(PrimaryConfig.class, PRIMARY_CONFIG_FILE_NAME);
                    if (load == null) {
                        throw new RuntimeException("Failed to initialize primary config, see above for further details");
                    }
                    this.primaryConfig = load;
                }
            }
        }

        return this.primaryConfig;
    }

    public Map<Key, CommandSettings> loadCommandSettings() {
        final @Nullable CommandConfig load = this.load(CommandConfig.class, COMMAND_SETTINGS_FILE_NAME);
        if (load == null) {
            throw new RuntimeException("Failed to initialize command settings, see above for further details");
        }
        return load.settings();
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

    public <T> @Nullable T load(final Class<T> clazz, final String fileName) {
        final Path file = this.dataDirectory.resolve(fileName);
        try {
            FileUtil.mkParentDirs(file);
        } catch (final IOException ex) {
            this.logger.error("Failed to create parent directories for '{}'", file, ex);
            return null;
        }

        final var loader = this.configurationLoader(file);

        try {
            final var node = loader.load();
            final @Nullable T config = node.get(clazz);
            if (config == null) {
                throw new ConfigurateException(node, "Failed to deserialize " + clazz.getName() + " from node");
            }
            node.set(clazz, config);
            loader.save(node);
            return config;
        } catch (final ConfigurateException exception) {
            this.logger.error("Failed to load config '{}'", file, exception);
            return null;
        }
    }

}
