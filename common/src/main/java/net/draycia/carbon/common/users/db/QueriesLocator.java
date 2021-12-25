/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;
import org.jdbi.v3.core.locator.internal.ClasspathBuilder;

public final class QueriesLocator {

    private static final Splitter SPLITTER = Splitter.on(';');
    private final ClasspathSqlLocator locator = ClasspathSqlLocator.create();
    private final DBType dbType;

    public QueriesLocator(final DBType dbType) {
        this.dbType = dbType;
    }

    public @NonNull String query(final @NonNull String name) {
        return this.locate(this.dbType.basePath() + name);
    }

    public @NonNull List<@NonNull String> queries(final @NonNull String name) {
        return SPLITTER.splitToList(this.locator.locate(this.dbType.basePath() + name));
    }

    private String locate(final String name) {
        return locator.getResource(
            CarbonChat.class.getClassLoader(),
            new ClasspathBuilder()
                .appendDotPath(name)
                .setExtension("sql")
                .build());
    }

}
