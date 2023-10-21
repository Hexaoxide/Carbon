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
package net.draycia.carbon.paper.hooks;

import com.google.inject.Inject;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CarbonPAPIPlaceholders extends PlaceholderExpansion {

    private final UserManager<?> userManager;

    @Inject
    public CarbonPAPIPlaceholders(final UserManager<?> userManager) {
        this.userManager = userManager;
        this.register();
    }

    @Override
    public String getIdentifier() {
        return "carbonchat";
    }

    @Override
    public String getAuthor() {
        return "Draycia";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer player, final String params) {
        if (params.endsWith("party")) {
            return mm(this.partyName(player));
        } else if (params.endsWith("party_l")) {
            return legacy(this.partyName(player));
        } else if (params.endsWith("nickname")) {
            return mm(this.nickname(player));
        } else if (params.endsWith("nickname_l")) {
            return legacy(this.nickname(player));
        } else if (params.endsWith("displayname")) {
            return mm(this.displayName(player));
        } else if (params.endsWith("displayname_l")) {
            return legacy(this.displayName(player));
        }

        return null;
    }

    private static String mm(final Component in) {
        return MiniMessage.miniMessage().serialize(in);
    }

    private static String legacy(final Component in) {
        return LegacyComponentSerializer.legacySection().serialize(in);
    }

    private Component partyName(final OfflinePlayer player) {
        final @Nullable Party party = this.userManager.user(player.getUniqueId()).thenCompose(CarbonPlayer::party).join();
        return party == null ? Component.empty() : party.name();
    }

    private Component displayName(final OfflinePlayer player) {
        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();
        return carbonPlayer.displayName();
    }

    private Component nickname(final OfflinePlayer player) {
        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();
        final @Nullable Component nickname = carbonPlayer.nickname();
        return nickname == null ? Component.text(carbonPlayer.username()) : nickname;
    }

}
