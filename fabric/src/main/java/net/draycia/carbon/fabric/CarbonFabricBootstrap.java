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
package net.draycia.carbon.fabric;

import com.google.inject.Guice;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Objects;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.common.util.DependencyDownloader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.apache.logging.log4j.LogManager;

public class CarbonFabricBootstrap implements ModInitializer {

    @Override
    public void onInitialize() {
        this.loadDependencies();

        final CarbonChatFabric carbonChat = Guice.createInjector(new CarbonChatFabricModule())
            .getInstance(CarbonChatFabric.class);
        CarbonChatProvider.register(carbonChat);
        carbonChat.onInitialize();
    }

    private void loadDependencies() {
        final DependencyDownloader downloader = new DependencyDownloader(
            LogManager.getLogger(this.getClass().getSimpleName()),
            FabricLoader.getInstance().getConfigDir().resolve("carbonchat").resolve("libraries")
        );

        try (final InputStream stream = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("carbon-dependencies.list"))) {
            downloader.load(stream);
        } catch (final IOException ex) {
            throw new RuntimeException("Couldn't load dependencies", ex);
        }

        for (final Path dep : downloader.resolve()) {
            addJarToClasspath(dep);
        }
    }

    private static void addJarToClasspath(final Path file) {
        try {
            FabricLauncherBase.getLauncher().propose(file.toUri().toURL());
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
