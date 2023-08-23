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

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DependencyDownloader {

    private final List<String> repositories = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();
    private final List<Relocation> relocations = new ArrayList<>();
    private final Logger logger;
    private final Path cacheDir;
    private final HttpClient client;

    public DependencyDownloader(
        final Logger logger,
        final Path cacheDir
    ) {
        this.logger = logger;
        this.cacheDir = cacheDir;
        this.client = HttpClient.newHttpClient();
    }

    public Set<Path> resolve() {
        final Set<Path> ret = ConcurrentHashMap.newKeySet();
        final AtomicBoolean didWork = new AtomicBoolean(false);

        final Runnable doingWork = () -> {
            if (didWork.compareAndSet(false, true)) {
                this.logger.info("Resolving dependencies...");
            }
        };

        final List<Callable<Void>> tasks = this.dependencies.stream().map(dep -> (Callable<Void>) () -> {
            try {
                final Path resolve = this.resolve(dep, doingWork);
                if (!resolve.getFileName().toString().endsWith(".jar")) {
                    return null;
                }
                if (this.relocations.isEmpty()) {
                    ret.add(resolve);
                    return null;
                }
                final Path relocated = resolve.resolveSibling(resolve.getFileName().toString().replace(".jar", "-relocated.jar"));
                ret.add(relocated);
                if (Files.isRegularFile(relocated)) {
                    return null;
                }
                doingWork.run();
                final Path output = relocated.resolveSibling(relocated.getFileName().toString() + ".tmp");
                Files.deleteIfExists(output);
                final JarRelocator relocator = new JarRelocator(resolve.toFile(), output.toFile(), this.relocations);
                //this.logger.info("relocating {}", resolve);
                relocator.run();
                Files.move(output, relocated);
                //this.logger.info("done relocating {}", resolve);
            } catch (final IOException | IllegalArgumentException e) {
                throw new RuntimeException("Exception resolving " + dep, e);
            }
            return null;
        }).toList();

        this.executeTasks(tasks);

        if (didWork.get()) {
            this.logger.info("Done resolving dependencies.");
        }

        return ret;
    }

    private void executeTasks(final List<Callable<Void>> tasks) {
        final ExecutorService executor = this.makeExecutor();
        try {
            final List<Future<Void>> result = executor.invokeAll(tasks, 10, TimeUnit.MINUTES);
            @Nullable RuntimeException err = null;
            for (final Future<Void> f : result) {
                try {
                    f.get();
                } catch (final ExecutionException | CancellationException e) {
                    if (err == null) {
                        err = new RuntimeException("Exception(s) resolving dependencies");
                    }
                    err.addSuppressed(e);
                }
            }
            if (err != null) {
                throw err;
            }
        } catch (final InterruptedException e) {
            this.logger.error("Interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            ConcurrentUtil.shutdownExecutor(executor, TimeUnit.MILLISECONDS, 2);
        }
    }

    private Path resolve(final Dependency dependency, final Runnable attemptingDownloadCallback) throws IOException {
        @Nullable Path resolved = null;
        final Path outputFile = this.cacheDir.resolve(String.format(
            "%s/%s/%s/%s-%s.jar",
            dependency.group.replace('.', '/'),
            dependency.name,
            dependency.version,
            dependency.name,
            dependency.version
        ));
        if (Files.exists(outputFile)) {
            if (checkHash(dependency, outputFile)) {
                return outputFile;
            }
            Files.delete(outputFile);
        }
        attemptingDownloadCallback.run();
        for (final String repository : this.repositories) {
            final String urlString = String.format(
                "%s%s/%s/%s/%s-%s.jar",
                repository,
                dependency.group.replace('.', '/'),
                dependency.name,
                dependency.version,
                dependency.name,
                dependency.version
            );

            final HttpRequest request;
            try {
                request = HttpRequest.newBuilder(new URI(urlString)).GET().build();
            } catch (final URISyntaxException e) {
                throw new RuntimeException(e);
            }
            final HttpResponse<Path> response;
            try {
                //this.logger.info("attempting to download " + urlString);
                response = this.client.send(request, HttpResponse.BodyHandlers.ofFile(
                    FileUtil.mkParentDirs(outputFile),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
                ));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (response == null || response.statusCode() != 200) {
                //this.logger.info("Download " + urlString + " failed");
                continue;
            }
            //this.logger.info("Download " + urlString + " success");
            resolved = response.body();
            break;
        }
        if (resolved == null) {
            throw new IllegalStateException(String.format("Could not resolve dependency %s from any of %s", dependency, this.repositories));
        }
        if (!checkHash(dependency, resolved)) {
            throw new IllegalStateException("Hash for downloaded file %s was incorrect (expected: %s, got: %s)".formatted(resolved, dependency.sha256(), FileUtil.hashString(resolved)));
        }
        return resolved;
    }

    private static boolean checkHash(final Dependency dependency, final Path resolved) throws IOException {
        return FileUtil.hashString(resolved).equalsIgnoreCase(dependency.sha256());
    }

    private ExecutorService makeExecutor() {
        return Executors.newFixedThreadPool(
            Math.min(4, Runtime.getRuntime().availableProcessors()),
            ConcurrentUtil.carbonThreadFactory(this.logger, this.getClass().getSimpleName())
        );
    }

    record Dependency(String group, String name, String version, String sha256) {}

    public void load(final InputStream dependencyList) throws IOException {
        try (
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dependencyList))
        ) {
            boolean doneWithRepos = false;
            boolean doneWithDeps = false;
            final Hasher hasher = Hashing.sha256().newHasher();
            for (;;) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.equals("end_deps")) {
                    doneWithDeps = true;
                    continue;
                } else if (line.equals("end_repos")) {
                    doneWithRepos = true;
                    continue;
                }
                hasher.putUnencodedChars(line);
                if (doneWithDeps) {
                    final String[] split = line.split(" ");
                    this.relocations.add(new Relocation(split[0], split[1]));
                } else if (doneWithRepos) {
                    final String[] split = line.split(" ");
                    final String[] coords = split[0].split(":");
                    this.dependencies.add(new Dependency(
                        coords[0],
                        coords[1],
                        coords[2],
                        split[1]
                    ));
                } else {
                    this.repositories.add(line);
                }
            }
            final Path hashFile = this.cacheDir.resolve("input.hash");
            Files.createDirectories(this.cacheDir);
            final byte[] hash = hasher.hash().asBytes();
            if (Files.exists(hashFile)) {
                final byte[] existing = Files.readAllBytes(hashFile);
                if (!Arrays.equals(existing, hash)) {
                    this.cleanCache();
                }
            } else {
                this.cleanCache();
            }
            Files.write(hashFile, hash);
        }
    }

    private void cleanCache() throws IOException {
        try (final Stream<Path> s = Files.walk(this.cacheDir)) {
            s.forEach(f -> {
                if (Files.isRegularFile(f) && f.getFileName().toString().endsWith(".jar")) {
                    try {
                        Files.delete(f);
                    } catch (final IOException e) {
                        Exceptions.rethrow(e);
                    }
                }
            });
        }
    }

}
