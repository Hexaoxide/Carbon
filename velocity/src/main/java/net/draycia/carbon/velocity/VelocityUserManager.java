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
package net.draycia.carbon.velocity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.proxy.ProxyServer;
import net.draycia.carbon.common.users.Backing;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public class VelocityUserManager extends PlatformUserManager<CarbonPlayerVelocity> {

    private final ProxyServer proxyServer;

    @Inject
    public VelocityUserManager(@Backing final UserManagerInternal<CarbonPlayerCommon> proxiedUserManager, final ProxyServer proxyServer) {
        super(proxiedUserManager);
        this.proxyServer = proxyServer;
    }

    @Override
    protected CarbonPlayerVelocity wrap(final CarbonPlayerCommon common) {
        return new CarbonPlayerVelocity(this.proxyServer, common);
    }

}
