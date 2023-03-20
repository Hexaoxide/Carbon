package net.draycia.carbon.common.users;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class MojangProfileResolver implements ProfileResolver {

    private final HttpClient client;
    private final Gson gson;

    public MojangProfileResolver() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<@Nullable UUID> resolveUUID(final String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .GET()
                    .build();

                final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response == null || response.statusCode() == 204 ) {
                    return null;
                }

                final BasicLookupResponse basicLookupResponse = gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>(){}.getType());

                return basicLookupResponse.id();
            } catch (final URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<@Nullable String> resolveName(final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")))
                    .GET()
                    .build();

                final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response == null || response.statusCode() == 204 ) {
                    return null;
                }

                final BasicLookupResponse basicLookupResponse = gson.fromJson(response.body(), new TypeToken<BasicLookupResponse>(){}.getType());

                return basicLookupResponse.name();
            } catch (final URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void shutdown() {

    }

    private record BasicLookupResponse(UUID id, String name) {

    }

}
