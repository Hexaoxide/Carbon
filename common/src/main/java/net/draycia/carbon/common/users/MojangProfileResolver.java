package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

@DefaultQualifier(NonNull.class)
public class MojangProfileResolver implements ProfileResolver {

    private final HttpClient client;
    private final Gson gson;

    private final ExecutorService executorService;

    private final Map<String, CompletableFuture<@Nullable UUID>> pendingUuidLookups = new HashMap<>();
    private final Map<UUID, CompletableFuture<@Nullable String>> pendingUsernameLookups = new HashMap<>();

    public MojangProfileResolver(final Logger logger) {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();

        this.executorService = Executors.newSingleThreadExecutor(ConcurrentUtil.carbonThreadFactory(logger, "MojangProfileResolver"));
    }

    @Override
    public synchronized CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return this.pendingUuidLookups.computeIfAbsent(username, $ -> {
            final CompletableFuture<@Nullable UUID> mojangLookup = CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.mojang.com/users/profiles/minecraft/" + username))
                        .GET()
                        .build();

                    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response == null || response.statusCode() != 200) {
                        return null;
                    }

                    final BasicLookupResponse basicLookupResponse = gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>(){}.getType());

                    return basicLookupResponse.id();
                } catch (final URISyntaxException | IOException | InterruptedException e) {
                    throw new RuntimeException(e);
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
            final CompletableFuture<@Nullable String> mojangLookup =  CompletableFuture.supplyAsync(() -> {
                try {
                    final HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.mojang.com/user/profile/" + uuid.toString().replace("-", "")))
                        .GET()
                        .build();

                    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response == null || response.statusCode() != 200) {
                        return null;
                    }

                    final BasicLookupResponse basicLookupResponse = gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>(){}.getType());

                    return basicLookupResponse.name();
                } catch (final URISyntaxException | IOException | InterruptedException e) {
                    throw new RuntimeException(e);
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

    @Override
    public void shutdown() {
        ConcurrentUtil.shutdownExecutor(this.executorService, TimeUnit.MILLISECONDS, 500);
    }

    private record BasicLookupResponse(UUID id, String name) {

    }

}
