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
package net.draycia.carbon.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CarbonDependencies {

    private CarbonDependencies() {
    }

    public static void load(
        final Path cacheDir,
        final Exceptions.CheckedConsumer<Path, Throwable> addToClasspath
    ) {
        final DependencyDownloader downloader = new DependencyDownloader(
            LogManager.getLogger(CarbonDependencies.class.getSimpleName()),
            cacheDir
        );

        try (final InputStream stream = Objects.requireNonNull(
            CarbonDependencies.class.getClassLoader().getResourceAsStream("carbon-dependencies.list"),
            "Could not get InputStream for carbon-dependencies.list"
        )) {
            downloader.load(stream);
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to load dependency list", ex);
        }

        final Set<Path> resolved = downloader.resolve();

        try {
            for (final Path dep : resolved) {
                addToClasspath.accept(dep);
            }
        } catch (final Error e) {
            throw e;
        } catch (final Throwable thr) {
            throw new RuntimeException("Failed to add dependencies to classpath", thr);
        }
    }

}
