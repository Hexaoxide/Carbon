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

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Calculates the SHA256 hash of {@code file} and returns it as a hex string.
     *
     * @param file file to hash
     * @return SHA256 hash string
     * @throws IOException              on I/O error
     * @throws IllegalArgumentException when {@code file} is not a regular file
     */
    public static String hashString(final Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Path '%s' is not a regular file, cannot generate hash string.".formatted(file));
        }
        final byte[] hash = com.google.common.io.Files.asByteSource(file.toFile()).hash(Hashing.sha256()).asBytes();
        return Strings.asHexString(hash);
    }

    /**
     * Lists directory entries in {@code path}.
     *
     * <p>If {@code path} does not exist, returns an empty list.</p>
     * <p>If {@code path} exists, but is not a directory, throws {@link IllegalArgumentException}</p>
     *
     * @param path directory
     * @return directory entries
     * @throws IllegalArgumentException when {@code path} exists but is not a directory
     * @throws UncheckedIOException     on I/O error
     */
    public static List<Path> listDirectoryEntries(final Path path) {
        return listDirectoryEntries(path, "*");
    }

    /**
     * Lists directory entries in {@code path} matching {@code glob}.
     *
     * <p>If {@code path} does not exist, returns an empty list.</p>
     * <p>If {@code path} exists, but is not a directory, throws {@link IllegalArgumentException}</p>
     *
     * @param path directory
     * @param glob glob pattern
     * @return matching directory entries
     * @throws IllegalArgumentException when {@code path} exists but is not a directory
     * @throws UncheckedIOException     on I/O error
     */
    public static List<Path> listDirectoryEntries(final Path path, final String glob) {
        if (!Files.exists(path)) {
            return List.of();
        } else if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path '%s' exists but is not a directory!".formatted(path));
        }

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, glob)) {
            final List<Path> ret = new ArrayList<>();
            stream.forEach(ret::add);
            return ret;
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to list directory entries matching '%s' in path '%s'.".formatted(glob, path), exception);
        }
    }

    /**
     * Attempts to create the parent directories of {@code path} if necessary.
     *
     * <p>Returns {@code path} when successful.</p>
     *
     * @param path path
     * @return {@code path}
     * @throws IOException on I/O error
     */
    public static Path mkParentDirs(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent != null && !Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final FileAlreadyExistsException ex) {
                if (!Files.isDirectory(parent)) {
                    throw ex;
                }
            }
        }
        return path;
    }

}
