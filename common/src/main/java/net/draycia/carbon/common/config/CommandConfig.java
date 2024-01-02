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
package net.draycia.carbon.common.config;

import java.util.Map;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.util.CloudUtils;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@DefaultQualifier(MonotonicNonNull.class)
public class CommandConfig {

    private Map<Key, CommandSettings> settings = CloudUtils.defaultCommandSettings();

    public CommandConfig() {

    }

    public CommandConfig(final Map<Key, CommandSettings> settings) {
        this.settings = settings;
    }

    public Map<Key, CommandSettings> settings() {
        return this.settings;
    }

}
