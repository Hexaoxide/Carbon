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

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.users.ProfileResolver;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class RenderForTagResolver implements TagResolver {

    private static final String TAG_NAME = "render_for";
    private final UserManagerInternal<?> users;
    private final ProfileResolver profileResolver;
    private final Provider<CarbonMessageRenderer> messageRenderer;
    private final Map<String, ?> resolvedPlaceholders;

    public interface Factory {

        RenderForTagResolver create(Map<String, ?> resolvedPlaceholders);
    }

    @AssistedInject
    private RenderForTagResolver(
        final UserManagerInternal<?> users,
        final ProfileResolver profileResolver,
        final Provider<CarbonMessageRenderer> messageRenderer,
        final @Assisted Map<String, ?> resolvedPlaceholders
    ) {
        this.users = users;
        this.profileResolver = profileResolver;
        this.messageRenderer = messageRenderer;
        this.resolvedPlaceholders = resolvedPlaceholders;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @Nullable Tag resolve(final String name, final ArgumentQueue arguments, final Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }

        final String renderFor = arguments.popOr("Missing username or UUID to render for").value();
        CompletableFuture<@Nullable ? extends CarbonPlayer> playerFuture;
        try {
            final UUID uuid = UUID.fromString(renderFor);
            playerFuture = this.users.user(uuid);
        } catch (final IllegalArgumentException ignore) {
            playerFuture = this.profileResolver.resolveUUID(renderFor).thenCompose(uuid -> {
                if (uuid != null) {
                    return this.users.user(uuid);
                }
                return CompletableFuture.completedFuture(null);
            });
        }

        final @Nullable CarbonPlayer player;
        try {
            player = playerFuture.join();
            if (player == null) {
                return null;
            }
        } catch (final CompletionException | CancellationException ignore) {
            return null;
        }

        final String value = arguments.popOr("Missing message value").value();
        if (value.equalsIgnoreCase("inserting")) {
            return Tag.inserting(
                this.messageRenderer.get().render(player, arguments.popOr("Missing message value").value(), this.resolvedPlaceholders, null, null)
            );
        } else if (value.equalsIgnoreCase("self_closing_inserting")) {
            return Tag.selfClosingInserting(
                this.messageRenderer.get().render(player, arguments.popOr("Missing message value").value(), this.resolvedPlaceholders, null, null)
            );
        } else {
            return Tag.selfClosingInserting(this.messageRenderer.get().render(player, value, this.resolvedPlaceholders, null, null));
        }
    }

    @Override
    public boolean has(final String name) {
        return name.equalsIgnoreCase(TAG_NAME);
    }
}
