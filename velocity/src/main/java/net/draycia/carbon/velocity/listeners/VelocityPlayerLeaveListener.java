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
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.velocity.CarbonVelocityBootstrap;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.draycia.carbon.common.users.PlayerUtils.saveExceptionHandler;

@DefaultQualifier(NonNull.class)
public final class VelocityPlayerLeaveListener implements VelocityListener<DisconnectEvent> {

    private final UserManagerInternal<?> userManager;
    private final Logger logger;

    @Inject
    public VelocityPlayerLeaveListener(
        final UserManagerInternal<?> userManager,
        final Logger logger
    ) {
        this.userManager = userManager;
        this.logger = logger;
    }

    @Override
    public void register(final EventManager eventManager, final CarbonVelocityBootstrap bootstrap) {
        eventManager.register(bootstrap, DisconnectEvent.class, this);
    }

    @Override
    public EventTask executeAsync(final DisconnectEvent event) {
        return EventTask.async(() -> {
            if (event.getLoginStatus() == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN) {
                return;
            }
            this.userManager.loggedOut(event.getPlayer().getUniqueId())
                .exceptionally(saveExceptionHandler(this.logger, event.getPlayer().getUsername(), event.getPlayer().getUniqueId()));
        });
    }

}
