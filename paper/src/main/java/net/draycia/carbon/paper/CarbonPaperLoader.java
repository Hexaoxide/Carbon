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

        maven.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());

        maven.addDependency(new Dependency(new DefaultArtifact("com.github.luben:zstd-jni:1.5.1-1"), null));
        maven.addDependency(new Dependency(new DefaultArtifact("com.google.protobuf:protobuf-java:3.21.12"), null));
        maven.addDependency(new Dependency(new DefaultArtifact("com.mysql:mysql-connector-j:8.0.31"), null));

        classpathBuilder.addLibrary(maven);
    }

}
