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

import java.util.Objects;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CarbonCommand {

    private static final String[] emptyAliases = new String[] {};

    private @Nullable CommandSettings commandSettings = null;

    public CommandSettings commandSettings() {
        return Objects.requireNonNullElseGet(this.commandSettings, this::defaultCommandSettings);
    }

    public void commandSettings(final @NonNull CommandSettings commandSettings) {
        this.commandSettings = commandSettings;
    }

    // TODO: Separate this from init so it's always called (when init's overridden)?
    public void init() {
        if (this.commandSettings().alternateRegistration()) {
            this.registerCommand(this.commandSettings().name(), emptyAliases);

            for (final String alias : this.commandSettings().aliases()) {
                this.registerCommand(alias, emptyAliases);
            }
        } else {
            this.registerCommand(this.commandSettings().name(), this.commandSettings().aliases());
        }

    }

    public void registerCommand(final String commandName, final String[] aliases) {

    }

    public abstract CommandSettings defaultCommandSettings();

    public abstract Key key();

}
