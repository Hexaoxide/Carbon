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
  UserChannelSettings channelSettings(@NonNull ChatChannel channel);

  boolean spyingwhispers();

  default void spyingWhispers(boolean spyingWhispers) {
    this.spyingWhispers(spyingWhispers, false);
  }

  void spyingWhispers(boolean spyingWhispers, boolean fromRemote);

  boolean muted();

  default void muted(boolean muted) {
    this.muted(muted, false);
  }

  void muted(boolean muted, boolean fromRemote);

  boolean shadowMuted();

  default void shadowMuted(boolean shadowMuted) {
    this.shadowMuted(shadowMuted, false);
  }

  void shadowMuted(boolean shadowMuted, boolean fromRemote);

  @Nullable
  UUID replyTarget();

  default void replyTarget(@Nullable UUID target) {
    this.replyTarget(target, false);
  }

  default void replyTarget(@Nullable ChatUser user) {
    this.replyTarget(user.uuid(), false);
  }

  void replyTarget(@Nullable UUID target, boolean fromRemote);

  default void replyTarget(@Nullable ChatUser user, boolean fromRemote) {
    this.replyTarget(user.uuid(), fromRemote);
  }

  boolean ignoringUser(@NonNull UUID uuid);

  void ignoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote);

  default void ignoringUser(@NonNull UUID uuid, boolean ignoring) {
    this.ignoringUser(uuid, ignoring, false);
  }

  default boolean ignoringUser(@NonNull ChatUser user) {
    return this.ignoringUser(user.uuid());
  }

  default void ignoringUser(@NonNull ChatUser user, boolean ignoring, boolean fromRemote) {
    this.ignoringUser(user.uuid(), ignoring, fromRemote);
  }

  default void ignoringUser(@NonNull ChatUser user, boolean ignoring) {
    this.ignoringUser(user.uuid(), ignoring, false);
  }

  void sendMessage(@NonNull final ChatUser sender, @NonNull String message);

}
