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

import java.sql.Driver;
import java.util.ServiceLoader;

public final class SQLDrivers {

    private SQLDrivers() {
    }

    public static void loadFrom(final ClassLoader loader) {
        ServiceLoader.load(Driver.class, loader).stream()
            .forEach(provider -> forceInit(provider.type()));
    }

    private static <T> Class<T> forceInit(final Class<T> klass) {
        try {
            Class.forName(klass.getName(), true, klass.getClassLoader());
        } catch (final ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        return klass;
    }

}
