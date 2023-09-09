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
package net.draycia.carbon.common.users.db;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.config.PrimaryConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;
import org.jdbi.v3.core.locator.internal.ClasspathBuilder;

@DefaultQualifier(NonNull.class)
public final class QueriesLocator {

    private static final String PREFIX = "queries/";
    private static final Splitter SPLITTER = Splitter.on(';');
    private final ClasspathSqlLocator locator = ClasspathSqlLocator.create();
    private final PrimaryConfig.StorageType storageType;
    private final Pattern templatePattern = Pattern.compile("\\{([^}]*?)}");
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public QueriesLocator(final PrimaryConfig.StorageType storageType) {
        this.storageType = storageType;
    }

    public List<String> queries(final String name) {
        return SPLITTER.splitToList(this.query(name));
    }

    public String query(final String name) {
        return this.locate(PREFIX + name);
    }

    private String locate(final String name) {
        return this.cache.computeIfAbsent(name, $ -> {
            final String sql = this.locator.getResource(
                CarbonChat.class.getClassLoader(),
                new ClasspathBuilder()
                    .appendDotPath(name)
                    .setExtension("sql")
                    .build());
            return this.processTemplates(sql);
        });
    }

    private String processTemplates(final String sql) {
        return this.templatePattern.matcher(sql).replaceAll(match -> {
            final String insideBraces = match.group(1);
            try {
                final int colonIndex = insideBraces.indexOf(':');
                String prefix = insideBraces.substring(0, colonIndex);
                final String content = insideBraces.substring(colonIndex + 1);
                boolean not = false;
                if (prefix.startsWith("!")) {
                    not = true;
                    prefix = prefix.substring(1);
                }
                final PrimaryConfig.StorageType storageType = PrimaryConfig.StorageType.valueOf(prefix);
                if (not) {
                    return storageType != this.storageType ? content : "";
                } else {
                    return storageType == this.storageType ? content : "";
                }
            } catch (final Exception ex) {
                return match.group(0);
            }
        });
    }

}
