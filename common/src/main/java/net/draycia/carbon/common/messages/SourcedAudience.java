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
package net.draycia.carbon.common.messages;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * An audience, where messages are sent from another Audience.
 */
@DefaultQualifier(NonNull.class)
public interface SourcedAudience extends ForwardingAudience.Single {

    /**
     * The source audience.
     *
     * @return source
     */
    Audience sender();

    /**
     * The recipient audience. The audience that this sourced audience forwards to.
     *
     * @return recipient
     */
    Audience recipient();

    @Override
    default Audience audience() {
        return this.recipient();
    }

    /**
     * Create a new {@link SourcedAudience} instance.
     *
     * @param sender sender
     * @param recipient recipient
     * @return sourced audience
     */
    static SourcedAudience of(final Audience sender, final Audience recipient) {
        return new SourcedAudienceImpl(sender, recipient);
    }

    /**
     * The empty {@link SourcedAudience}, with an empty sender and recipient.
     *
     * @return the empty sourced audience
     */
    static SourcedAudience empty() {
        return SourcedAudienceImpl.EMPTY;
    }

}
