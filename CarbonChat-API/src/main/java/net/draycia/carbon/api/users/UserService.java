package net.draycia.carbon.api.users;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface UserService {

  @Nullable
  ChatUser wrap(UUID uuid);

  @Nullable
  ChatUser wrapIfLoaded(@NonNull UUID uuid);

  @Nullable
  ChatUser refreshUser(@NonNull UUID uuid);

  void onDisable();

  void invalidate(@NonNull ChatUser user);

  void validate(@NonNull ChatUser user);

}
