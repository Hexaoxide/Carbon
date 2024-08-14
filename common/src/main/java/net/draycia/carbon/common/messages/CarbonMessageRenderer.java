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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.moonshine.message.IMessageRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CarbonMessageRenderer implements IMessageRenderer<Audience, String, Component, Object> {

    private final RenderForTagResolver.Factory renderForTagResolver;

    protected CarbonMessageRenderer(final RenderForTagResolver.Factory renderForTagResolver) {
        this.renderForTagResolver = renderForTagResolver;
    }

    public final IMessageRenderer<SourcedAudience, String, Component, Object> asSourced() {
        return this::render;
    }

    @Override
    public final Component render(
        final Audience receiver,
        final String intermediateMessage,
        final Map<String, ?> resolvedPlaceholders,
        final @Nullable Method method,
        final @Nullable Type owner
    ) {
        final TagResolver.Builder builder = TagResolver.builder();
        addResolved(builder, resolvedPlaceholders);
        builder.resolver(this.renderForTagResolver.create(resolvedPlaceholders));
        return this.render(receiver, intermediateMessage, builder);
    }

    protected abstract Component render(
        Audience receiver,
        String intermediateMessage,
        TagResolver.Builder resolverBuilder
    );

    @SuppressWarnings("PatternValidation")
    private static void addResolved(final TagResolver.Builder tagResolver, final Map<String, ?> resolvedPlaceholders) {
        for (final var entry : resolvedPlaceholders.entrySet()) {
            if (entry.getValue() instanceof Tag tag) {
                tagResolver.tag(entry.getKey(), tag);
            } else if (entry.getValue() instanceof TagResolver resolver) {
                tagResolver.resolver(resolver);
            } else {
                throw new IllegalArgumentException(entry.getValue().toString());
            }
        }
    }

}
