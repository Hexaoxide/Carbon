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
import com.google.inject.Injector;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.util.Objects;
import java.util.UUID;
import net.draycia.carbon.api.channels.ChannelRegistry;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class MiniPlaceholdersExpansion {

    private final UserManager<?> userManager;
    private final ChannelRegistry channels;

    private static byte miniPlaceholdersLoaded = -1;

    @Inject
    private MiniPlaceholdersExpansion(
        final UserManager<?> userManager,
        final ChannelRegistry channels
    ) {
        this.userManager = userManager;
        this.channels = channels;
    }

    public static void register(final Injector injector) {
        if (miniPlaceholdersLoaded()) {
            injector.getInstance(MiniPlaceholdersExpansion.class).registerExpansion();
        }
    }

    public static boolean miniPlaceholdersLoaded() {
        if (miniPlaceholdersLoaded == -1) {
            try {
                final String name = MiniPlaceholders.class.getName();
                Objects.requireNonNull(name);
                miniPlaceholdersLoaded = 1;
            } catch (final NoClassDefFoundError error) {
                miniPlaceholdersLoaded = 0;
            }
        }
        return miniPlaceholdersLoaded == 1;
    }

    private void registerExpansion() {
        final Expansion expansion = Expansion.builder("carbonchat")
            .filter(audience -> audience.get(Identity.UUID).isPresent())
            .audiencePlaceholder("party", (audience, queue, ctx) ->
                Tag.selfClosingInserting(this.partyName(id(audience))))
            .audiencePlaceholder("nickname", (audience, queue, ctx) ->
                Tag.selfClosingInserting(this.nickname(id(audience))))
            .audiencePlaceholder("displayname", (audience, queue, ctx) ->
                Tag.selfClosingInserting(this.displayName(id(audience))))
            .audiencePlaceholder("channel_key", (audience, queue, ctx) ->
                Tag.preProcessParsed(this.selectedChannelKey(id(audience))))
            .build();
        expansion.register();
    }

    private static UUID id(final Audience audience) {
        return audience.get(Identity.UUID).orElseThrow();
    }

    private Component partyName(final UUID id) {
        final @Nullable Party party = this.userManager.user(id).thenCompose(CarbonPlayer::party).join();
        return party == null ? Component.empty() : party.name();
    }

    private Component displayName(final UUID id) {
        final CarbonPlayer carbonPlayer = this.userManager.user(id).join();
        return carbonPlayer.displayName();
    }

    private Component nickname(final UUID id) {
        final CarbonPlayer carbonPlayer = this.userManager.user(id).join();
        final @Nullable Component nickname = carbonPlayer.nickname();
        return nickname == null ? Component.text(carbonPlayer.username()) : nickname;
    }

    private String selectedChannelKey(final UUID id) {
        final CarbonPlayer carbonPlayer = this.userManager.user(id).join();
        final @Nullable ChatChannel selected = carbonPlayer.selectedChannel();
        if (selected != null) {
            return selected.key().asString();
        }
        return this.channels.defaultKey().asString();
    }

}
