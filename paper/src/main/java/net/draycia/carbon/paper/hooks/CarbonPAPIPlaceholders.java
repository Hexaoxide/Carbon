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
import net.draycia.carbon.api.users.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CarbonPAPIPlaceholders extends PlaceholderExpansion {

    private final UserManager<?> userManager;

    @Inject
    public CarbonPAPIPlaceholders(final UserManager<?> userManager) {
        this.userManager = userManager;
        this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "carbonchat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Draycia";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        final CarbonPlayer carbonPlayer = this.userManager.user(player.getUniqueId()).join();

        final Component nickname = CarbonPlayer.renderName(carbonPlayer);

        if (params.endsWith("nickname")) {
            return MiniMessage.miniMessage().serialize(nickname);
        } else if (params.endsWith("nickname_l")) {
            return LegacyComponentSerializer.legacySection().serialize(nickname);
        }

        return null;
    }

}
