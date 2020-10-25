package net.draycia.carbon.api.users;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface UserService<T extends PlayerUser> {

  @NonNull
  T wrap(UUID uuid);

  @Nullable // Maybe NonNull?
  <C extends ConsoleUser> C consoleUser();

  @Nullable
  T wrapIfLoaded(@NonNull UUID uuid);

  @Nullable
  T refreshUser(@NonNull UUID uuid);

  void onDisable();

  void invalidate(@NonNull T user);

  void validate(@NonNull T user);

  @NonNull Iterable<@NonNull T> onlineUsers();

}
