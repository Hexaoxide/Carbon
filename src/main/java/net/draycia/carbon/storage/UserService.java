package net.draycia.carbon.storage;

import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface UserService {

  @Nullable
  ChatUser wrap(String name);

  @Nullable
  ChatUser wrap(OfflinePlayer player);

  @Nullable
  ChatUser wrap(UUID uuid);

  @Nullable
  default ChatUser wrapIfLoaded(@NonNull final OfflinePlayer player) {
    return this.wrapIfLoaded(player.getUniqueId());
  }

  @Nullable
  ChatUser wrapIfLoaded(@NonNull UUID uuid);

  @Nullable
  ChatUser refreshUser(@NonNull UUID uuid);

  void onDisable();

  void invalidate(@NonNull ChatUser user);

  void validate(@NonNull ChatUser user);

}
