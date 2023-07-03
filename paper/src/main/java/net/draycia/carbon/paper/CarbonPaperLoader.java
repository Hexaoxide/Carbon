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
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.springframework.lang.NonNull;

@DefaultQualifier(NonNull.class)
public class CarbonPaperLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver maven = new MavenLibraryResolver();

        try (
            final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("carbon-dependencies.list");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)))
        ) {
            boolean doneWithRepos = false;
            int repoIndex = 0;
            for (;;) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("end_deps")) {
                    break;
                } else if (line.equals("end_repos")) {
                    doneWithRepos = true;
                    continue;
                }
                if (doneWithRepos) {
                    final String[] split = line.split(" ");
                    maven.addDependency(new Dependency(new DefaultArtifact(split[0]), null));
                } else {
                    maven.addRepository(new RemoteRepository.Builder("carbon" + repoIndex++, "default", line).build());
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        classpathBuilder.addLibrary(maven);
    }

}
