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
package net.draycia.carbon.common.command;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class CommandSettings {

    private boolean enabled = true;
    private String name = "";
    private String[] aliases = new String[0];

    public CommandSettings() {

    }

    public CommandSettings(final boolean enabled, final String name, final String... aliases) {
        this.enabled = enabled;
        this.name = name;
        this.aliases = aliases;
    }

    public CommandSettings(final String name, final String... aliases) {
        this(true, name, aliases);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public String name() {
        return this.name;
    }

    public String[] aliases() {
        return this.aliases;
    }

}
