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
package net.draycia.carbon.paper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.Server;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CarbonServerPaper implements CarbonServer, ForwardingAudience.Single {

    private final Server server;
    private final PaperUserManager userManager;

    @Inject
    private CarbonServerPaper(final Server server, final PaperUserManager userManager) {
        this.server = server;
        this.userManager = userManager;
    }

    @Override
    public Audience audience() {
        return this.server;
    }

    @Override
    public Audience console() {
        return this.server.getConsoleSender();
    }

    @Override
    public List<? extends CarbonPlayer> players() {
        return this.server.getOnlinePlayers().stream()
            .map(bukkit -> this.userManager.user(bukkit.getUniqueId()).getNow(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }

}
