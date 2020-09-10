package net.draycia.carbon.storage;

import net.draycia.carbon.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface ChatUser extends Audience {

  @Nullable
  Player player();

  @NonNull
  OfflinePlayer offlinePlayer();

  @NonNull
  UUID uuid();

  boolean online();

  @Nullable
  String nickname();

  default void nickname(@NonNull String nickname) {
    this.nickname(nickname, false);
  }

  void nickname(@Nullable String nickname, boolean fromRemote);

  @Nullable
  ChatChannel selectedChannel();

  default void selectedChannel(@NonNull ChatChannel channel) {
    this.selectedChannel(channel, false);
  }

  void selectedChannel(@NonNull ChatChannel channel, boolean fromRemote);

  void clearSelectedChannel();

  @NonNull
  UserChannelSettings getChannelSettings(@NonNull ChatChannel channel);

  boolean isSpyingWhispers();

  default void setSpyingWhispers(boolean spyingWhispers) {
    this.setSpyingWhispers(spyingWhispers, false);
  }

  void setSpyingWhispers(boolean spyingWhispers, boolean fromRemote);

  boolean isMuted();

  default void setMuted(boolean muted) {
    this.setMuted(muted, false);
  }

  void setMuted(boolean muted, boolean fromRemote);

  boolean isShadowMuted();

  default void setShadowMuted(boolean shadowMuted) {
    this.setShadowMuted(shadowMuted, false);
  }

  void setShadowMuted(boolean shadowMuted, boolean fromRemote);

  @Nullable
  UUID getReplyTarget();

  default void setReplyTarget(@Nullable UUID target) {
    this.setReplyTarget(target, false);
  }

  default void setReplyTarget(@Nullable ChatUser user) {
    this.setReplyTarget(user.uuid(), false);
  }

  void setReplyTarget(@Nullable UUID target, boolean fromRemote);

  default void setReplyTarget(@Nullable ChatUser user, boolean fromRemote) {
    this.setReplyTarget(user.uuid(), fromRemote);
  }

  boolean isIgnoringUser(@NonNull UUID uuid);

  void setIgnoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote);

  default void setIgnoringUser(@NonNull UUID uuid, boolean ignoring) {
    this.setIgnoringUser(uuid, ignoring, false);
  }

  default boolean isIgnoringUser(@NonNull ChatUser user) {
    return this.isIgnoringUser(user.uuid());
  }

  default void setIgnoringUser(@NonNull ChatUser user, boolean ignoring, boolean fromRemote) {
    this.setIgnoringUser(user.uuid(), ignoring, fromRemote);
  }

  default void setIgnoringUser(@NonNull ChatUser user, boolean ignoring) {
    this.setIgnoringUser(user.uuid(), ignoring, false);
  }

  void sendMessage(@NonNull ChatUser sender, @NonNull String message);

}
