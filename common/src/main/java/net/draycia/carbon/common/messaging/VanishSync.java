/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
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
package net.draycia.carbon.common.messaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Detects local vanish state changes and broadcasts them to the rest of the network.
 *
 * <p>{@link CarbonPlayer#vanished()} only reflects live state for players on the querying server, so
 * remote servers rely on the state synced through the player-presence packets. Join/quit seed the
 * initial state; this task keeps it up to date when players are hidden or shown at runtime.</p>
 *
 * <p>Rather than hooking a specific vanish plugin's events (there is no common Bukkit event for vanish
 * changes), this polls the same {@link CarbonPlayer#vanished()} value CarbonChat already reads, so it
 * works with any supported vanish plugin (PremiumVanish, SuperVanish, VanishNoPacket, ...) and on any
 * platform.</p>
 */
@DefaultQualifier(NonNull.class)
@Singleton
public final class VanishSync {

    public static final long POLL_INTERVAL_SECONDS = 3;

    private final CarbonServer server;
    private final Provider<MessagingManager> messaging;
    private final PacketFactory packetFactory;
    private final Map<UUID, Boolean> lastKnown = new ConcurrentHashMap<>();

    @Inject
    private VanishSync(
        final CarbonServer server,
        final Provider<MessagingManager> messaging,
        final PacketFactory packetFactory
    ) {
        this.server = server;
        this.messaging = messaging;
        this.packetFactory = packetFactory;
    }

    public void pollAndBroadcast() {
        final Set<UUID> online = new HashSet<>();

        for (final CarbonPlayer player : this.server.players()) {
            final UUID uuid = player.uuid();
            online.add(uuid);

            final boolean vanished = player.vanished();
            final @Nullable Boolean previous = this.lastKnown.put(uuid, vanished);

            // First observation: the join packet already seeds the state, so only correct it when the
            // player turns out to be vanished (in case vanish was applied after the join packet was sent).
            final boolean changed = previous == null ? vanished : previous != vanished;
            if (changed) {
                this.messaging.get().queuePacket(() -> this.packetFactory.addLocalPlayerPacket(uuid, player.username(), vanished));
            }
        }

        this.lastKnown.keySet().retainAll(online);
    }

}
