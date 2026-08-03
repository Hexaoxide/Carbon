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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@NullMarked
public class CommandConfig {

    private @MonotonicNonNull @Nullable Map<@MonotonicNonNull @Nullable Key, @MonotonicNonNull @Nullable CommandSettings> settings =
        CloudUtils.defaultCommandSettings();

    public CommandConfig() {

    }

    public CommandConfig(
        final @MonotonicNonNull @Nullable Map<@MonotonicNonNull @Nullable Key, @MonotonicNonNull @Nullable CommandSettings> settings
    ) {
        this.settings = settings;
    }

    public @MonotonicNonNull @Nullable Map<@MonotonicNonNull @Nullable Key, @MonotonicNonNull @Nullable CommandSettings> settings() {
        return this.settings;
    }

}
