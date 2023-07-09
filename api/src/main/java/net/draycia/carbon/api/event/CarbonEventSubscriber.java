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
package net.draycia.carbon.api.event;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

/**
 * An EventSubscriber.
 *
 * @param <T> CarbonEvent implementations
 * @since 2.1.0
 */
@DefaultQualifier(NonNull.class)
public interface CarbonEventSubscriber<T extends CarbonEvent> {

    /**
     * Invokes this event consumer.
     *
     * @param event the event
     * @throws Throwable if an exception is thrown
     * @since 1.0.0
     */
    void on(final @NotNull T event) throws Throwable;

}
