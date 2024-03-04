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

import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.argument.PlayerSuggestions;
import net.draycia.carbon.common.messaging.packets.LocalPlayerChangePacket;
import net.draycia.carbon.common.messaging.packets.LocalPlayersPacket;
import net.draycia.carbon.common.util.Exceptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Eventually consistent store of who is on each server in the network (besides self).
 *
 * <p>Currently used for username suggestions and whispers.</p>
 */
@DefaultQualifier(NonNull.class)
@Singleton
public final class NetworkUsers implements PlayerSuggestions {

    private final CarbonServer server;
    private final Map<UUID, Map<UUID, String>> map = new ConcurrentHashMap<>();
    private final UserManager<? extends CarbonPlayer> userManager;
    private final ProfileCache profileCache;

    @Inject
    private NetworkUsers(
        final CarbonServer server,
        final UserManager<?> userManager,
        final ProfileCache profileCache
    ) {
        this.server = server;
        this.userManager = userManager;
        this.profileCache = profileCache;
    }

    public void handlePacket(final LocalPlayerChangePacket packet) {
        final Map<UUID, String> serverMap = this.map.computeIfAbsent(packet.getSender(), $ -> new ConcurrentHashMap<>());

        switch (packet.changeType()) {
            case ADD -> {
                serverMap.put(packet.playerId(), packet.playerName());
                this.profileCache.cache(packet.playerId(), packet.playerName());
            }
            case REMOVE -> serverMap.remove(packet.playerId());
        }

        this.map.values().removeIf(Map::isEmpty);
    }

    public void handlePacket(final LocalPlayersPacket packet) {
        if (packet.players().isEmpty()) {
            this.map.remove(packet.getSender());
        } else {
            final Map<UUID, String> serverMap = this.map.computeIfAbsent(packet.getSender(), $ -> new ConcurrentHashMap<>());
            serverMap.clear();
            serverMap.putAll(packet.players());

            packet.players().forEach(this.profileCache::cache);
        }
    }

    // PlayerSuggestions impl
    @Override
    public List<String> apply(final CommandContext<Commander> ctx, final String input) {
        final Commander commander = ctx.getSender();

        final List<? extends CarbonPlayer> local = this.server.players();

        if (!(commander instanceof PlayerCommander player)) {
            return Stream.concat(local.stream().map(CarbonPlayer::username), this.map.values().stream().flatMap(m -> m.values().stream()))
                .distinct()
                .toList();
        }
        final CarbonPlayer carbonPlayer = player.carbonPlayer();

        final List<? extends CompletableFuture<? extends CarbonPlayer>> remotePlayerFutures =
            this.map.values().stream()
                .flatMap(m -> m.keySet().stream())
                .map(this.userManager::user)
                .toList(); // collect to ensure we request all futures before waiting
        final CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(remotePlayerFutures.toArray(CompletableFuture[]::new));
        try {
            combinedFuture.get(50, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException ignore) {
        } catch (final Exception e) {
            throw Exceptions.rethrow(e);
        }
        final Stream<? extends CarbonPlayer> remote = remotePlayerFutures.stream()
            .map(future -> future.getNow(null))
            .filter(Objects::nonNull);

        return Stream.concat(local.stream(), remote)
            .filter(carbonPlayer::awareOf)
            .map(CarbonPlayer::username)
            .distinct()
            .toList();
    }

    public boolean online(final CarbonPlayer player) {
        if (player.online()) {
            return true;
        }
        return this.map.values().stream().anyMatch(server -> server.containsKey(player.uuid()));
    }

    public boolean online(final UUID uuid) {
        final @Nullable CarbonPlayer player = this.server.players().stream()
            .filter(it -> it.uuid().equals(uuid))
            .findFirst()
            .orElse(null);
        return player != null || this.map.values().stream().anyMatch(server -> server.containsKey(uuid));
    }

}
