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
package net.draycia.carbon.common.users;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.Party;
import net.draycia.carbon.common.config.ConfigManager;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.draycia.carbon.common.messaging.MessagingManager;
import net.draycia.carbon.common.messaging.packets.InvalidatePartyInvitePacket;
import net.draycia.carbon.common.messaging.packets.PacketFactory;
import net.draycia.carbon.common.messaging.packets.PartyInvitePacket;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class PartyInvites {

    private final Map<UUID, Cache<UUID, UUID>> pendingInvites = new ConcurrentHashMap<>();
    private final Provider<MessagingManager> messaging;
    private final PacketFactory packetFactory;
    private final UserManagerInternal<?> users;
    private final Logger logger;
    private final CarbonMessages messages;
    private final ConfigManager config;

    @Inject
    private PartyInvites(
        final Provider<MessagingManager> messaging,
        final PacketFactory packetFactory,
        final UserManagerInternal<?> users,
        final Logger logger,
        final CarbonMessages messages,
        final ConfigManager config
    ) {
        this.messaging = messaging;
        this.packetFactory = packetFactory;
        this.users = users;
        this.logger = logger;
        this.messages = messages;
        this.config = config;
    }

    public void sendInvite(final UUID from, final UUID to, final UUID party) {
        final Cache<UUID, UUID> cache = this.orCreateInvitesFor(to);
        cache.put(from, party);
        this.clean();

        this.messaging.get().queuePacket(() -> this.packetFactory.partyInvite(from, to, party));
    }

    public void invalidateInvite(final UUID from, final UUID to) {
        this.invalidateInvite_(from, to);

        this.messaging.get().queuePacket(() -> this.packetFactory.invalidatePartyInvite(from, to));
    }

    private void invalidateInvite_(final UUID from, final UUID to) {
        final @Nullable Cache<UUID, UUID> cache = this.invitesFor(to);
        if (cache != null) {
            cache.invalidate(from);
        }
        this.clean();
    }

    public @Nullable Cache<UUID, UUID> invitesFor(final UUID recipient) {
        return this.pendingInvites.get(recipient);
    }

    private Cache<UUID, UUID> orCreateInvitesFor(final UUID recipient) {
        return this.pendingInvites.computeIfAbsent(recipient, $ -> this.makeCache());
    }

    private Cache<UUID, UUID> makeCache() {
        return Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(this.config.primaryConfig().partyChat().expireInvitesAfterSeconds)).build();
    }

    public void handle(final InvalidatePartyInvitePacket pkt) {
        this.invalidateInvite_(pkt.from(), pkt.to());
        this.clean();
    }

    private void clean() {
        this.pendingInvites.values().removeIf(it -> it.asMap().size() == 0);
    }

    public void handle(final PartyInvitePacket pkt) {
        final @Nullable Cache<UUID, UUID> cache = this.orCreateInvitesFor(pkt.to());
        cache.put(pkt.from(), pkt.party());
        this.clean();

        final CompletableFuture<? extends CarbonPlayer> to = this.users.user(pkt.to());
        final CompletableFuture<? extends CarbonPlayer> from = this.users.user(pkt.to());
        final CompletableFuture<Party> party = this.users.party(pkt.party());

        CompletableFuture.allOf(to, from, party).thenRun(() -> {
            if (to.join().online()) {
                this.messages.receivedPartyInvite(to.join(), from.join().displayName(), from.join().username(), party.join().name());
            }
        }).whenComplete(($, thr) -> {
            if (thr != null) {
                this.logger.warn("Exception handling {}", pkt, thr);
            }
        });
    }

}
