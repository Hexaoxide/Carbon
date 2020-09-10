package net.draycia.carbon.storage;

import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface UserService {

  @Nullable
  ChatUser wrap(@NonNull String name);

  @Nullable
  ChatUser wrap(@NonNull OfflinePlayer player);

  @Nullable
  ChatUser wrap(@NonNull UUID uuid);

  @Nullable
  default ChatUser wrapIfLoaded(@NonNull OfflinePlayer player) {
    return wrapIfLoaded(player.getUniqueId());
  }

  @Nullable
  ChatUser wrapIfLoaded(@NonNull UUID uuid);

  @Nullable
  ChatUser refreshUser(@NonNull UUID uuid);

  void onDisable();

  void invalidate(@NonNull ChatUser user);

  void validate(@NonNull ChatUser user);
}
