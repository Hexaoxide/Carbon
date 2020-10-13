package net.draycia.carbon.api.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CarbonUser extends Audience {

  boolean hasPermission(@NonNull String permission);

  @NonNull String name();

  @Nullable ChatChannel selectedChannel();

  default void selectedChannel(@NonNull final ChatChannel channel) {
    this.selectedChannel(channel, false);
  }

  void selectedChannel(@NonNull ChatChannel channel, boolean fromRemote);

  void clearSelectedChannel();

}
