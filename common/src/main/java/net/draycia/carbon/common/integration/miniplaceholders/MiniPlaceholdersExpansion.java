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
package net.draycia.carbon.common.integration.miniplaceholders;

import com.google.inject.Inject;
import io.github.miniplaceholders.api.Expansion;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class MiniPlaceholdersExpansion {

    private final UserManagerInternal<?> userManager;
    private final ChannelRegistry channels;

    @Inject
    private MiniPlaceholdersExpansion(
        final UserManagerInternal<?> userManager,
        final ChannelRegistry channels
    ) {
        this.userManager = userManager;
        this.channels = channels;
    }

    public void registerExpansion() {
        final Expansion expansion = Expansion.builder("carbonchat")
            .audiencePlaceholder("party", (audience, queue, ctx) -> {
                if (!hasId(audience)) {
                    return null;
                }
                return Tag.selfClosingInserting(this.partyName(id(audience)));
            })
            .audiencePlaceholder("nickname", (audience, queue, ctx) -> {
                if (!hasId(audience)) {
                    return null;
                }
                return Tag.selfClosingInserting(this.nickname(id(audience)));
            })
            .audiencePlaceholder("displayname", (audience, queue, ctx) -> {
                if (!hasId(audience)) {
                    return null;
                }
                return Tag.selfClosingInserting(this.displayName(id(audience)));
            })
            .audiencePlaceholder("channel_key", (audience, queue, ctx) -> {
                if (!hasId(audience)) {
                    return null;
                }
                return Tag.preProcessParsed(this.selectedChannelKey(id(audience)));
            })
            .build();
        expansion.register();
    }

    private static boolean hasId(final Audience audience) {
        return audience.get(Identity.UUID).isPresent();
    }

    private static UUID id(final Audience audience) {
        return audience.get(Identity.UUID).orElseThrow();
    }

    private Component partyName(final UUID id) {
        final @Nullable CarbonPlayer player = this.userManager.cachedUser(id);
        if (player == null) {
            return Component.empty();
        }
        final @Nullable Party party = player.party().getNow(null);
        return party == null ? Component.empty() : party.name();
    }

    private Component displayName(final UUID id) {
        final @Nullable CarbonPlayer player = this.userManager.cachedUser(id);
        return player != null ? player.displayName() : Component.empty();
    }

    private Component nickname(final UUID id) {
        final @Nullable CarbonPlayer player = this.userManager.cachedUser(id);
        if (player == null) {
            return Component.empty();
        }
        final @Nullable Component nickname = player.nickname();
        return nickname == null ? Component.text(player.username()) : nickname;
    }

    private String selectedChannelKey(final UUID id) {
        final @Nullable CarbonPlayer player = this.userManager.cachedUser(id);
        if (player == null) {
            return this.channels.defaultKey().asString();
        }
        final @Nullable ChatChannel selected = player.selectedChannel();
        return selected != null ? selected.key().asString() : this.channels.defaultKey().asString();
    }

}
