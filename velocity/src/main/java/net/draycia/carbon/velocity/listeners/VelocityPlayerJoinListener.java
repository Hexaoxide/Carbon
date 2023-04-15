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
package net.draycia.carbon.velocity.listeners;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.draycia.carbon.velocity.VelocityUserManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public class VelocityPlayerJoinListener {

    private final VelocityUserManager userManager;
    private final Logger logger;

    @Inject
    public VelocityPlayerJoinListener(
        final VelocityUserManager userManager,
        final Logger logger
    ) {
        this.userManager = userManager;
        this.logger = logger;
    }

    @Subscribe
    public void onPlayerJoin(final LoginEvent event) {
        this.userManager.user(event.getPlayer().getUniqueId()).exceptionally(thr -> {
            this.logger.warn("Exception handling player join", thr);
            return null;
        });
    }

    @Subscribe
    public void onPlayerLeave(final DisconnectEvent event) {
        this.userManager.loggedOut(event.getPlayer().getUniqueId()).exceptionally(ex -> {
            this.logger.warn("Exception saving data for player " + event.getPlayer().getUsername() + " with uuid " + event.getPlayer().getUniqueId());
            return null;
        });
    }

}
