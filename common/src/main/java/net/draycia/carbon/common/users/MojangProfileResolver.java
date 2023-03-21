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
package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.draycia.carbon.common.util.ConcurrentUtil;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

// todo rate limit handling
@DefaultQualifier(NonNull.class)
public class MojangProfileResolver implements ProfileResolver {

    private final HttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<@Nullable UUID>> pendingUuidLookups = new HashMap<>();
    private final Map<UUID, CompletableFuture<@Nullable String>> pendingUsernameLookups = new HashMap<>();

    @Inject
    private MojangProfileResolver(final Logger logger) {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(2, ConcurrentUtil.carbonThreadFactory(logger, "MojangProfileResolver"));
    }

    @Override
    public synchronized CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return this.pendingUuidLookups.computeIfAbsent(username, $ -> {
            final CompletableFuture<@Nullable UUID> mojangLookup = CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = createRequest(
                        "https://api.mojang.com/users/profiles/minecraft/" + username);

                    return this.sendRequest(request, BasicLookupResponse::id);
                } catch (final Exception e) {
                    throw new RuntimeException("Exception resolving UUID for name " + username, e);
                }
            }, this.executorService);

            mojangLookup.whenComplete((result, $$$) -> {
                synchronized (this) {
                    this.pendingUuidLookups.remove(username);
                }
            });

            return mojangLookup;
        });
    }

    @Override
    public synchronized CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return this.pendingUsernameLookups.computeIfAbsent(uuid, $ -> {
            final CompletableFuture<@Nullable String> mojangLookup = CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = createRequest(
                        "https://api.mojang.com/user/profile/" + uuid.toString().replace("-", ""));

                    return this.sendRequest(request, BasicLookupResponse::name);
                } catch (final Exception e) {
                    throw new RuntimeException("Exception resolving name for UUID " + uuid, e);
                }
            }, this.executorService);

            mojangLookup.whenComplete((result, $$$) -> {
                synchronized (this) {
                    this.pendingUsernameLookups.remove(uuid);
                }
            });

            return mojangLookup;
        });
    }

    private static HttpRequest createRequest(final String uri) throws URISyntaxException {
        return HttpRequest.newBuilder()
            .uri(new URI(uri))
            .GET()
            .build();
    }

    private <T> @Nullable T sendRequest(final HttpRequest request, final Function<BasicLookupResponse, @Nullable T> mapper) throws IOException, InterruptedException {
        final HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response == null) {
            throw new RuntimeException("Null response for request " + request);
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Received non-200 response code for request " + request);
        }

        final BasicLookupResponse basicLookupResponse = this.gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>() {}.getType());
        if (basicLookupResponse == null) {
            throw new RuntimeException("Malformed response body for request " + request + ": '" + response.body() + "'");
        }
        return mapper.apply(basicLookupResponse);
    }

    @Override
    public void shutdown() {
        ConcurrentUtil.shutdownExecutor(this.executorService, TimeUnit.MILLISECONDS, 500);
    }

    private record BasicLookupResponse(UUID id, String name) {

    }

}
