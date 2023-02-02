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
package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

/**
 * An audience, where messages are sent from another Audience.
 *
 * @since 2.0.0
 */
public record SourcedAudience(Audience sender, Audience recipient) implements ForwardingAudience.Single {

    public static final SourcedAudience EMPTY = new SourcedAudience(Audience.empty(), Audience.empty());

    /**
     * An empty {@link SourcedAudience}, with an empty sender and recipient.
     *
     * @return an empty sourced audience
     * @since 2.0.0
     */
    public static SourcedAudience empty() {
        return EMPTY;
    }

    @Override
    public @NotNull Audience audience() {
        return this.recipient;
    }

}
