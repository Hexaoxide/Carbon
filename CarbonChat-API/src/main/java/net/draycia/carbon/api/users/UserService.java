package net.draycia.carbon.api.users;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserService<T extends PlayerUser> {

  @Nullable <C extends ConsoleUser> C consoleUser();

  @NonNull T wrap(@NonNull UUID uuid);

  @NonNull CompletableFuture<T> wrapLater(@NonNull UUID uuid);

  @Nullable T wrapIfLoaded(@NonNull UUID uuid);

  @Nullable T refreshUser(@NonNull UUID uuid);

  void onDisable();

  void invalidate(@NonNull T user);

  void validate(@NonNull T user);

  @NonNull Iterable<@NonNull T> onlineUsers();

}
