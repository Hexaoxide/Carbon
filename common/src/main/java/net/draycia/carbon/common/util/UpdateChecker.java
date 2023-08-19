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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public record UpdateChecker(Logger logger) {

    private static final String GITHUB_REPO = "Hexaoxide/Carbon";
    private static final String UPDATE_CHECKER_FETCHING_VERSION_INFORMATION = "Fetching version information...";
    private static final String DEV_BUILD_NOTICE = "This is a development version of CarbonChat (<version>)!";
    private static final String UPDATE_CHECKER_BEHIND_RELEASES = "CarbonChat is <behind> version(s) out of date (<version>).";
    private static final String UPDATE_CHECKER_DOWNLOAD_RELEASE = "Download the latest release (<latest>) from <link>";
    private static final String RELEASE_DOWNLOADS_URL = "https://modrinth.com/plugin/carbon/versions";
    private static final Gson GSON = new GsonBuilder().create();

    public void checkVersion() {
        this.logger.info(UPDATE_CHECKER_FETCHING_VERSION_INFORMATION);

        final @Nullable Manifest manifest = manifest(UpdateChecker.class); // we expect to be shaded into platform jars
        if (manifest == null) {
            this.logger.warn("Failed to locate manifest, cannot check for updates.");
            return;
        }

        final String currentVersion = manifest.getMainAttributes().getValue("carbon-version");

        final Releases releases;
        try {
            releases = this.fetchReleases();
        } catch (final IOException e) {
            this.logger.warn("Failed to list releases, cannot check for updates.", e);
            return;
        }

        final String ver = "v" + currentVersion;
        if (releases.releaseList().get(0).equals(ver)) {
            return;
        }
        if (currentVersion.contains("-SNAPSHOT")) {
            this.logger.info(DEV_BUILD_NOTICE.replace("<version>", ver));
        } else {
            final int versionsBehind = releases.releaseList().indexOf(ver);
            this.logger.info(
                UPDATE_CHECKER_BEHIND_RELEASES
                    .replace("<behind>", String.valueOf(versionsBehind == -1 ? "?" : versionsBehind))
                    .replace("<version>", ver)
            );
        }
        this.logger.info(
            UPDATE_CHECKER_DOWNLOAD_RELEASE
                .replace("<latest>", releases.releaseList().get(0))
                .replace("<link>", RELEASE_DOWNLOADS_URL) // , releases.releaseUrls().get(releases.releaseList().get(0)))
        );
    }

    private Releases fetchReleases() throws IOException {
        final JsonArray result;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/%s/releases".formatted(GITHUB_REPO)).openStream(), StandardCharsets.UTF_8))) {
            result = GSON.fromJson(reader, JsonArray.class);
        }

        final Map<String, String> versionMap = new LinkedHashMap<>();
        for (final JsonElement element : result) {
            versionMap.put(
                element.getAsJsonObject().get("tag_name").getAsString(),
                element.getAsJsonObject().get("html_url").getAsString()
            );
        }
        return new Releases(new ArrayList<>(versionMap.keySet()), versionMap);
    }

    private record Releases(List<String> releaseList, Map<String, String> releaseUrls) {
    }

    public static @Nullable Manifest manifest(final Class<?> clazz) {
        final String classLocation = "/" + clazz.getName().replace(".", "/") + ".class";
        final @Nullable URL resource = clazz.getResource(classLocation);
        if (resource == null) {
            return null;
        }
        final String classFilePath = resource.toString().replace("\\", "/");
        final String archivePath = classFilePath.substring(0, classFilePath.length() - classLocation.length());
        try (final InputStream stream = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
            return new Manifest(stream);
        } catch (final IOException ex) {
            return null;
        }
    }

}
