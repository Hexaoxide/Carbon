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
  Player asPlayer();

  @NonNull
  OfflinePlayer asOfflinePlayer();

  @NonNull
  UUID getUUID();

  boolean isOnline();

  @Nullable
  String getNickname();

  default void setNickname(@NonNull String nickname) {
    this.setNickname(nickname, false);
  }

  void setNickname(@Nullable String nickname, boolean fromRemote);

  @Nullable
  ChatChannel getSelectedChannel();

  default void setSelectedChannel(@NonNull ChatChannel channel) {
    this.setSelectedChannel(channel, false);
  }

  void setSelectedChannel(@NonNull ChatChannel channel, boolean fromRemote);

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
    this.setReplyTarget(user.getUUID(), false);
  }

  void setReplyTarget(@Nullable UUID target, boolean fromRemote);

  default void setReplyTarget(@Nullable ChatUser user, boolean fromRemote) {
    this.setReplyTarget(user.getUUID(), fromRemote);
  }

  boolean isIgnoringUser(@NonNull UUID uuid);

  void setIgnoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote);

  default void setIgnoringUser(@NonNull UUID uuid, boolean ignoring) {
    this.setIgnoringUser(uuid, ignoring, false);
  }

  default boolean isIgnoringUser(@NonNull ChatUser user) {
    return this.isIgnoringUser(user.getUUID());
  }

  default void setIgnoringUser(@NonNull ChatUser user, boolean ignoring, boolean fromRemote) {
    this.setIgnoringUser(user.getUUID(), ignoring, fromRemote);
  }

  default void setIgnoringUser(@NonNull ChatUser user, boolean ignoring) {
    this.setIgnoringUser(user.getUUID(), ignoring, false);
  }

  void sendMessage(@NonNull ChatUser sender, @NonNull String message);

}
