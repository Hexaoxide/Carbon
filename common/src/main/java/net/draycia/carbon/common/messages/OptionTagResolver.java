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
package net.draycia.carbon.common.messages;

import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.Nullable;

@DefaultQualifier(NonNull.class)
public final class OptionTagResolver implements TagResolver {

    private final String name;
    private final boolean state;

    public OptionTagResolver(final String name, final boolean state) {
        this.name = name;
        this.state = state;
    }

    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        final Tag.Argument t = arguments.popOr("Missing option 1");
        final Tag.Argument f = arguments.popOr("Missing option 2");
        return Tag.preProcessParsed(this.state ? t.value() : f.value());
    }

    @Override
    public boolean has(final String name) {
        return name.equals(this.name);
    }

}
