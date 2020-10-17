package net.draycia.carbon.api.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlayerUser extends CarbonUser {

  @NonNull UUID uuid();

  boolean online();

  @Nullable String nickname();

  default void nickname(@Nullable final String nickname) {
    this.nickname(nickname, false);
  }

  void nickname(@Nullable String nickname, boolean fromRemote);

  @Nullable String displayName();

  void displayName(@Nullable String displayName);

  @NonNull String parsePlaceholders(@NonNull String input);

  @NonNull UserChannelSettings channelSettings(@NonNull ChatChannel channel);

  @NonNull Map<@NonNull String, @NonNull ? extends UserChannelSettings> channelSettings();

  boolean spyingWhispers();

  default void spyingWhispers(final boolean spyingWhispers) {
    this.spyingWhispers(spyingWhispers, false);
  }

  void spyingWhispers(boolean spyingWhispers, boolean fromRemote);

  boolean muted();

  default void muted(final boolean muted) {
    this.muted(muted, false);
  }

  void muted(boolean muted, boolean fromRemote);

  boolean shadowMuted();

  default void shadowMuted(final boolean shadowMuted) {
    this.shadowMuted(shadowMuted, false);
  }

  void shadowMuted(boolean shadowMuted, boolean fromRemote);

  @Nullable UUID replyTarget();

  default void replyTarget(@Nullable final UUID target) {
    this.replyTarget(target, false);
  }

  default void replyTarget(@Nullable final PlayerUser user) {
    this.replyTarget(user.uuid(), false);
  }

  void replyTarget(@Nullable UUID target, boolean fromRemote);

  default void replyTarget(@Nullable final PlayerUser user, final boolean fromRemote) {
    this.replyTarget(user.uuid(), fromRemote);
  }

  boolean ignoringUser(@NonNull UUID uuid);

  void ignoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote);

  default void ignoringUser(@NonNull final UUID uuid, final boolean ignoring) {
    this.ignoringUser(uuid, ignoring, false);
  }

  default boolean ignoringUser(@NonNull final PlayerUser user) {
    return this.ignoringUser(user.uuid());
  }

  default void ignoringUser(@NonNull final PlayerUser user, final boolean ignoring, final boolean fromRemote) {
    this.ignoringUser(user.uuid(), ignoring, fromRemote);
  }

  default void ignoringUser(@NonNull final PlayerUser user, final boolean ignoring) {
    this.ignoringUser(user.uuid(), ignoring, false);
  }

  @NonNull Iterable<CarbonUser> ignoredChatUsers();

  @NonNull Iterable<UUID> ignoredUsers();

  boolean hasGroup(@NonNull final String group);

  boolean hasGroup(@NonNull final Group group);

  @NonNull Collection<@NonNull Group> groups();

  @Nullable Group primaryGroup();

  void sendMessage(@NonNull final PlayerUser sender, @NonNull String message);

}
