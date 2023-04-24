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
package net.draycia.carbon.common.util;

import net.draycia.carbon.api.users.CarbonPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class EmptyAudienceWithPointers implements ForwardingAudience.Single {

    private final Pointers pointers;

    private EmptyAudienceWithPointers(final Pointers pointers) {
        this.pointers = pointers;
    }

    @Override
    public Audience audience() {
        return Audience.empty();
    }

    @Override
    public Pointers pointers() {
        return this.pointers;
    }

    public static EmptyAudienceWithPointers forCarbonPlayer(final CarbonPlayer player) {
        return new EmptyAudienceWithPointers(Pointers.builder()
            .withStatic(Identity.UUID, player.uuid())
            .withStatic(Identity.NAME, player.username())
            .withDynamic(Identity.DISPLAY_NAME, player::displayName)
            .build());
    }

}
