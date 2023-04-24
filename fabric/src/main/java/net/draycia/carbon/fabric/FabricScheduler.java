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
package net.draycia.carbon.fabric;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.PlatformScheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class FabricScheduler implements PlatformScheduler {

    private final MinecraftServerHolder serverHolder;

    @Inject
    private FabricScheduler(final MinecraftServerHolder serverHolder) {
        this.serverHolder = serverHolder;
    }

    @Override
    public void scheduleForPlayer(final CarbonPlayer carbonPlayer, final Runnable runnable) {
        this.serverHolder.requireServer().execute(runnable);
    }

}
