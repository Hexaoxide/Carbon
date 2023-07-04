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
package net.draycia.carbon.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import net.draycia.carbon.common.util.DependencyDownloader;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.springframework.lang.NonNull;

@DefaultQualifier(NonNull.class)
public class CarbonPaperLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final DependencyDownloader downloader = new DependencyDownloader(
            LogManager.getLogger(this.getClass().getSimpleName()),
            classpathBuilder.getContext().getDataDirectory().resolve("libraries")
        );
        try (
            final InputStream stream = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("carbon-dependencies.list"))
        ) {
            downloader.load(stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        for (final Path path : downloader.resolve()) {
            classpathBuilder.addLibrary(new JarLibrary(path));
        }
    }

}
