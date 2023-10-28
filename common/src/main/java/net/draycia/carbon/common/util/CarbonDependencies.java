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

import java.nio.file.Path;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.gremlin.runtime.DependencyCache;
import xyz.jpenilla.gremlin.runtime.DependencyResolver;
import xyz.jpenilla.gremlin.runtime.DependencySet;

@DefaultQualifier(NonNull.class)
public final class CarbonDependencies {

    private CarbonDependencies() {
    }

    public static Set<Path> resolve(final Path cacheDir) {
        final DependencySet deps = DependencySet.readFromClasspathResource(
            CarbonDependencies.class.getClassLoader(), "carbon-dependencies.txt");
        final DependencyCache cache = new DependencyCache(cacheDir);
        final Logger logger = LoggerFactory.getLogger(CarbonDependencies.class.getSimpleName());
        final Set<Path> files;
        try (final DependencyResolver downloader = new DependencyResolver(logger)) {
            files = downloader.resolve(deps, cache).jarFiles();
        }
        cache.cleanup();
        return files;
    }

}
