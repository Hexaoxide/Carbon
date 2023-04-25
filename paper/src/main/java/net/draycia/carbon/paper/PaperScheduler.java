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
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.PlatformScheduler;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@Singleton
@DefaultQualifier(NonNull.class)
public final class PaperScheduler implements PlatformScheduler {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (final ClassNotFoundException exception) {
            folia = false;
        }
        FOLIA = folia;
    }

    private final JavaPlugin plugin;
    private final Server server;
    private final @Nullable Folia folia;

    @Inject
    private PaperScheduler(final JavaPlugin plugin, final Server server) {
        this.plugin = plugin;
        this.server = server;
        this.folia = FOLIA ? new Folia() : null;
    }

    public void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable) {
        if (this.folia != null) {
            this.folia.scheduleForPlayer(carbonPlayer, runnable);
            return;
        }

        if (this.server.isPrimaryThread()) {
            runnable.run();
        } else {
            this.server.getScheduler().runTask(this.plugin, runnable);
        }
    }

    // inner class to avoid Guice trying to load ScheduledTask when scanning for methods to inject,
    // and finding the synthetic method generated for our ScheduledTask consumer lambda
    private final class Folia implements PlatformScheduler {

        @Override
        public void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable) {
            final @Nullable Player player = PaperScheduler.this.server.getPlayer(carbonPlayer.uuid());

            if (player == null) {
                runnable.run();
                return;
            }

            if (PaperScheduler.this.server.isOwnedByCurrentRegion(player)) {
                runnable.run();
            } else {
                player.getScheduler().run(PaperScheduler.this.plugin, $ -> runnable.run(), null);
            }
        }

    }

}
