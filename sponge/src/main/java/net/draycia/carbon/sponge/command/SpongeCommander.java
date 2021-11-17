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
package net.draycia.carbon.sponge.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;

@DefaultQualifier(NonNull.class)
public interface SpongeCommander extends Commander, ForwardingAudience.Single {

    static SpongeCommander from(final CommandCause commandCause) {
        return new SpongeCommanderImpl(commandCause);
    }

    @NonNull CommandCause commandCause();

    record SpongeCommanderImpl(CommandCause commandCause) implements SpongeCommander {

        @Override
        public @NotNull Audience audience() {
            return this.commandCause.audience();
        }

    }

}
