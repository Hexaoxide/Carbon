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
package net.draycia.carbon.common.command.exception;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class ComponentException extends RuntimeException implements ComponentMessageThrowable {

    private static final long serialVersionUID = 132203031250316968L;

    private final @Nullable Component message;

    protected ComponentException(final @Nullable Component message) {
        this.message = message;
    }

    public static ComponentException withoutMessage() {
        return new ComponentException(null);
    }

    public static ComponentException withMessage(final ComponentLike message) {
        return new ComponentException(message.asComponent());
    }

    @Override
    public @Nullable Component componentMessage() {
        return this.message;
    }

    @Override
    public String getMessage() {
        return PlainTextComponentSerializer.plainText().serializeOr(this.message, "No message.");
    }

}
