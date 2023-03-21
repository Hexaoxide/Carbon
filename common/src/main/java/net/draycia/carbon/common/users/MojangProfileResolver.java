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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.draycia.carbon.common.util.ConcurrentUtil;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

// todo rate limit handling
@DefaultQualifier(NonNull.class)
public final class MojangProfileResolver implements ProfileResolver {

    private final HttpClient client;
    private final Gson gson;
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<@Nullable BasicLookupResponse>> pendingUuidLookups = new HashMap<>();
    private final Map<UUID, CompletableFuture<@Nullable BasicLookupResponse>> pendingUsernameLookups = new HashMap<>();

    @Inject
    private MojangProfileResolver(final Logger logger) {
        this.client = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();
        this.executorService = Executors.newFixedThreadPool(2, ConcurrentUtil.carbonThreadFactory(logger, "MojangProfileResolver"));
    }

    @Override
    public synchronized CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        if (username.length() > 25 || username.length() < 1) { // Invalid names
            return CompletableFuture.completedFuture(null);
        }
        return this.pendingUuidLookups.computeIfAbsent(username, $ -> {
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
    public synchronized CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return this.pendingUsernameLookups.computeIfAbsent(uuid, $ -> {
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
            // todo
            throw new IllegalStateException("Rate limited");
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
    }

    private record BasicLookupResponse(UUID id, String name) {

    }

    private static final class UUIDTypeAdapter extends TypeAdapter<UUID> {

        private static final String REGEX = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";

        private UUIDTypeAdapter() {
        }

        @Override
        public void write(final JsonWriter out, final UUID value) throws IOException {
            out.value(fromUUID(value));
        }

        @Override
        public UUID read(final JsonReader in) throws IOException {
            return fromString(in.nextString());
        }

        private static String fromUUID(final UUID value) {
            return value.toString().replace("-", "");
        }

        private static UUID fromString(final String input) {
            return UUID.fromString(input.replaceFirst(REGEX, "$1-$2-$3-$4-$5"));
        }

    }

}
