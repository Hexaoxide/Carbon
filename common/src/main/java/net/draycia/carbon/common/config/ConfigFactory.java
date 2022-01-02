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
package net.draycia.carbon.common.config;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.ForCarbon;
import net.draycia.carbon.common.events.CarbonReloadEvent;
import net.draycia.carbon.common.serialisation.gson.LocaleSerializerConfigurate;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

@DefaultQualifier(NonNull.class)
public class ConfigFactory {

    private final Path dataDirectory;
    private final LocaleSerializerConfigurate locale;

    private @Nullable PrimaryConfig primaryConfig = null;
    private @Nullable CommandConfig commandSettings = null;

    @Inject
    public ConfigFactory(
        final CarbonChat carbonChat,
        @ForCarbon final Path dataDirectory,
        final LocaleSerializerConfigurate locale
    ) {
        this.dataDirectory = dataDirectory;
        this.locale = locale;

        carbonChat.eventHandler().subscribe(CarbonReloadEvent.class, event -> {
            this.reloadPrimaryConfig();
        });
    }

    public @Nullable PrimaryConfig reloadPrimaryConfig() {
        try {
            this.primaryConfig = this.load(PrimaryConfig.class, "config.conf");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return this.primaryConfig;
    }

    public @Nullable PrimaryConfig primaryConfig() {
        if (this.primaryConfig == null) {
            return this.reloadPrimaryConfig();
        }

        return this.primaryConfig;
    }

    public @Nullable CommandConfig loadCommandSettings() {
        try {
            this.commandSettings = this.load(CommandConfig.class, "command-settings.conf");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return this.commandSettings;
    }

    public @Nullable CommandConfig commandSettings() {
        if (this.commandSettings == null) {
            return this.loadCommandSettings();
        }

        return this.commandSettings;
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
