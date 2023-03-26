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
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.draycia.carbon.common.util.ConcurrentUtil;
import net.draycia.carbon.common.util.FastUuidSansHyphens;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class MojangProfileResolver implements ProfileResolver {

    private final HttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<@Nullable BasicLookupResponse>> pendingUuidLookups = new HashMap<>();
    private final Map<UUID, CompletableFuture<@Nullable BasicLookupResponse>> pendingUsernameLookups = new HashMap<>();
    private final ProfileCache cache;
    private final RateLimiter globalRateLimit;
    private final RateLimiter uuidToProfileRateLimit;

    @Inject
    private MojangProfileResolver(final Logger logger, final ProfileCache cache) {
        this.client = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();
        this.executorService = Executors.newFixedThreadPool(2, ConcurrentUtil.carbonThreadFactory(logger, "MojangProfileResolver"));
        this.cache = cache;
        this.globalRateLimit = new RateLimiter(600);
        this.uuidToProfileRateLimit = new RateLimiter(200);
    }

    @Override
    public synchronized CompletableFuture<@Nullable UUID> resolveUUID(final String username, final boolean cacheOnly) {
        if (username.length() > 25 || username.length() < 1) { // Invalid names
            return CompletableFuture.completedFuture(null);
        }
        if (cacheOnly || this.cache.hasCachedEntry(username)) {
            return CompletableFuture.completedFuture(this.cache.cachedId(username));
        }
        return this.pendingUuidLookups.computeIfAbsent(username, $ -> {
            if (!this.globalRateLimit.canSubmit()) {
                return CompletableFuture.completedFuture(null);
            }
            final CompletableFuture<@Nullable BasicLookupResponse> mojangLookup = CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = createRequest(
                        "https://api.mojang.com/users/profiles/minecraft/" + username);

                    return this.sendRequest(request);
                } catch (final Exception e) {
                    throw new RuntimeException("Exception resolving UUID for name " + username, e);
                }
            }, this.executorService);

            mojangLookup.whenComplete((result, $$$) -> {
                synchronized (this) {
                    this.cache.cache(result == null ? null : result.id(), username);
                    this.pendingUuidLookups.remove(username);
                }
            });

            return mojangLookup;
        }).thenApply(response -> {
            if (response == null) {
                return null;
            }
            return response.id();
        });
    }

    @Override
    public synchronized CompletableFuture<@Nullable String> resolveName(final UUID uuid, final boolean cacheOnly) {
        if (cacheOnly || this.cache.hasCachedEntry(uuid)) {
            return CompletableFuture.completedFuture(this.cache.cachedName(uuid));
        }
        return this.pendingUsernameLookups.computeIfAbsent(uuid, $ -> {
            if (!this.globalRateLimit.canSubmit() || !this.uuidToProfileRateLimit.canSubmit()) {
                return CompletableFuture.completedFuture(null);
            }
            final CompletableFuture<@Nullable BasicLookupResponse> mojangLookup = CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = createRequest(
                        "https://api.mojang.com/user/profile/" + uuid.toString().replace("-", ""));

                    return this.sendRequest(request);
                } catch (final Exception e) {
                    throw new RuntimeException("Exception resolving name for UUID " + uuid, e);
                }
            }, this.executorService);

            mojangLookup.whenComplete((result, $$$) -> {
                synchronized (this) {
                    this.cache.cache(uuid, result == null ? null : result.name());
                    this.pendingUsernameLookups.remove(uuid);
                }
            });

            return mojangLookup;
        }).thenApply(response -> {
            if (response == null) {
                return null;
            }
            return response.name();
        });
    }

    private static HttpRequest createRequest(final String uri) throws URISyntaxException {
        return HttpRequest.newBuilder()
            .uri(new URI(uri))
            .GET()
            .build();
    }

    private @Nullable BasicLookupResponse sendRequest(final HttpRequest request) throws IOException, InterruptedException {
        final HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response == null) {
            throw new RuntimeException("Null response for request " + request);
        } else if (response.statusCode() == 429) {
            throw new RuntimeException("Got rate-limited by Mojang, could not fulfill request: " + request);
        } else if (response.statusCode() == 404) {
            // No such profile
            return null;
        } else if (response.statusCode() == 400) {
            // Invalid name/UUID
            return null;
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("Received non-200 response code (" + response.statusCode() + ") for request " + request + ": " + response.body());
        }

        final BasicLookupResponse basicLookupResponse = this.gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>() {}.getType());
        if (basicLookupResponse == null) {
            throw new RuntimeException("Malformed response body for request " + request + ": '" + response.body() + "'");
        }
        return basicLookupResponse;
    }

    @Override
    public void shutdown() {
        ConcurrentUtil.shutdownExecutor(this.executorService, TimeUnit.MILLISECONDS, 500);
        this.globalRateLimit.shutdown();
        this.uuidToProfileRateLimit.shutdown();
    }

    private record BasicLookupResponse(UUID id, String name) {

    }

    private static final class UUIDTypeAdapter extends TypeAdapter<UUID> {

        private UUIDTypeAdapter() {
        }

        @Override
        public void write(final JsonWriter out, final UUID value) throws IOException {
            out.value(FastUuidSansHyphens.toString(value));
        }

        @Override
        public UUID read(final JsonReader in) throws IOException {
            final String input = in.nextString();
            return FastUuidSansHyphens.parseUuid(input);
        }

    }

    private static final class RateLimiter {

        private final AtomicInteger available;
        private final Timer timer;

        private RateLimiter(final int perTenMinutes) {
            this.timer = new Timer("CarbonChat " + this);
            this.available = new AtomicInteger(perTenMinutes);
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    RateLimiter.this.available.set(perTenMinutes);
                }
            }, 0L, Duration.ofMinutes(10).toMillis());
        }

        boolean canSubmit() {
            return this.available.getAndDecrement() >= 0;
        }

        void shutdown() {
            this.timer.cancel();
        }

    }

}
