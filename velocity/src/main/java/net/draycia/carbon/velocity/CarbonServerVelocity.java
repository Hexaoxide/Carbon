/*
 * CarbonChat
 *
 * Copyright (c) 2021 Josua Parks (Vicarious)
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.UuidUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.draycia.carbon.api.CarbonServer;
import net.draycia.carbon.api.users.ComponentPlayerResult;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.users.CarbonPlayerCommon;
import net.draycia.carbon.common.util.FastUuidSansHyphens;
import net.draycia.carbon.velocity.users.CarbonPlayerVelocity;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@DefaultQualifier(NonNull.class)
public final class CarbonServerVelocity implements CarbonServer, ForwardingAudience.Single {

    private final ProxyServer server;
    private final UserManager<CarbonPlayerVelocity> userManager;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    @Inject
    private CarbonServerVelocity(final ProxyServer server, final UserManager<CarbonPlayerCommon> userManager) {
        this.server = server;
        this.userManager = new VelocityUserManager(userManager, server);
    }

    @Override
    public @NotNull Audience audience() {
        return Audience.audience(this.console(), Audience.audience(this.players()));
    }

    @Override
    public Audience console() {
        return this.server.getConsoleCommandSource();
    }

    @Override
    public List<CarbonPlayerVelocity> players() {
        final var players = new ArrayList<CarbonPlayerVelocity>();

        for (final var player : this.server.getAllPlayers()) {
            final ComponentPlayerResult<CarbonPlayerVelocity> carbonPlayer = this.userManager.carbonPlayer(player.getUniqueId()).join();

            if (carbonPlayer.player() != null) {
                players.add(carbonPlayer.player());
            }
        }

        return players;
    }

    @Override
    public UserManager<CarbonPlayerVelocity> userManager() {
        return this.userManager;
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            final var serverPlayer = this.server.getPlayer(username);

            if (serverPlayer.isPresent()) {
                return serverPlayer.get().getUniqueId();
            }

            try {
                final @Nullable JsonObject json = this.queryMojang(new URI("https://api.mojang.com/users/profiles/minecraft/" + username));

                return FastUuidSansHyphens.parseUuid(json.get("id").getAsString());
            } catch (final URISyntaxException exception) {
                exception.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final var serverPlayer = this.server.getPlayer(uuid);

            if (serverPlayer.isPresent()) {
                return serverPlayer.get().getUsername();
            }

            try {
                final @Nullable JsonObject json = this.queryMojang(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + UuidUtils.toUndashed(uuid)));

                return json.get("name").getAsString();
            } catch (final URISyntaxException exception) {
                exception.printStackTrace();
            }

            return null;
        });
    }

    private @Nullable JsonObject queryMojang(final URI uri) {
        final HttpRequest request = HttpRequest
            .newBuilder(uri)
            .GET()
            .build();

        try {
            final HttpResponse<String> response =
                this.client.send(request, HttpResponse.BodyHandlers.ofString());
            final String mojangResponse = response.body();

            return this.gson.fromJson(mojangResponse, JsonObject.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
