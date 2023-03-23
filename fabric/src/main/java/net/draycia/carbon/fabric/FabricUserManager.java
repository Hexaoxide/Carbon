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
import net.draycia.carbon.common.users.Backing;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.users.PlatformUserManager;
import net.draycia.carbon.common.users.UserManagerInternal;
import net.draycia.carbon.fabric.users.CarbonPlayerFabric;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class FabricUserManager extends PlatformUserManager<CarbonPlayerFabric> {

    private final CarbonChatFabric carbonChatFabric;

    @Inject
    private FabricUserManager(final @Backing UserManagerInternal<CarbonPlayerCommon> proxiedUserManager, final CarbonChatFabric carbonChatFabric) {
        super(proxiedUserManager);
        this.carbonChatFabric = carbonChatFabric;
    }

    @Override
    protected CarbonPlayerFabric wrap(final CarbonPlayerCommon common) {
        return new CarbonPlayerFabric(common, this.carbonChatFabric);
    }

    @Override
    protected void updateTransientLoadedStatus(final CarbonPlayerFabric wrapped) {
        wrapped.carbonPlayerCommon().markTransientLoaded(wrapped.player().isEmpty());
    }

}
