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
package net.draycia.carbon.common.integration;

import java.lang.reflect.Type;
import net.draycia.carbon.common.config.ConfigManager;

public interface Integration {

    boolean eligible();

    void register();

    interface ConfigMeta {
        Type type();

        String name();

        record ConfigMetaRecord(Type type, String name) implements ConfigMeta {}
    }

    static ConfigMeta configMeta(final String name, final Type type) {
        return new ConfigMeta.ConfigMetaRecord(type, name);
    }

    default <C> C config(final ConfigManager configManager, final ConfigMeta meta) {
        return configManager.primaryConfig().integrations().config(meta);
    }

}
