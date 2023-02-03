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
package net.draycia.carbon.paper.command;

import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PaperCommander extends Commander, ForwardingAudience.Single {

    static PaperCommander from(final CommandSender sender) {
        return new PaperCommanderImpl(sender);
    }

    CommandSender commandSender();

    record PaperCommanderImpl(CommandSender commandSender) implements PaperCommander {

        @Override
        public Audience audience() {
            return this.commandSender;
        }

        @Override
        public boolean hasPermission(final String permission) {
            return this.commandSender.hasPermission(permission);
        }

    }

}
