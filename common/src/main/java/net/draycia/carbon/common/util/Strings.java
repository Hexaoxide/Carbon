/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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

import com.google.common.base.Suppliers;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Strings {

    private static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/([A-Za-z0-9\\-._~!$&'()*+,;=:@/]|%[0-9A-Fa-f]{2})*)?");
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("^[a-z][a-z0-9+\\-.]*:");
    public static final Supplier<TextReplacementConfig> URL_REPLACEMENT_CONFIG = Suppliers.memoize(
        () -> TextReplacementConfig.builder()
            .match(DEFAULT_URL_PATTERN)
            .replacement(url -> {
                String clickUrl = url.content();
                if (!URL_SCHEME_PATTERN.matcher(clickUrl).find()) {
                    clickUrl = "http://" + clickUrl;
                }

                try {
                    final URI ignored = new URI(clickUrl); // just to validate that the uri is valid
                    return url.clickEvent(ClickEvent.openUrl(clickUrl));
                } catch (final URISyntaxException ignored) {
                    return url;
                }
            })
            .build()
    );

    private Strings() {
    }

    public static @Nullable String trim(final @Nullable String s) {
        return s == null ? null : s.trim();
    }

    public static String asHexString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (final byte b : bytes) {
            sb.append("%02x".formatted(b & 0xFF));
        }
        return sb.toString();
    }

}
