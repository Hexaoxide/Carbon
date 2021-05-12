package net.draycia.carbon.api.users;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface UserManager {

  @Nullable CarbonPlayer carbonPlayer(final @NonNull UUID uuid);

  @Nullable CarbonPlayer carbonPlayer(final @NonNull String username);

}
