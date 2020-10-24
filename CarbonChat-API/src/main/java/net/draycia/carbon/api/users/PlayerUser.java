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

  @NonNull String nickname();

  default void nickname(final @Nullable String nickname) {
    this.nickname(nickname, false);
  }

  void nickname(@Nullable String nickname, boolean fromRemote);

  @NonNull String displayName();

  void displayName(@Nullable String displayName);

  @NonNull String name();

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

  default void replyTarget(final @Nullable UUID target) {
    this.replyTarget(target, false);
  }

  default void replyTarget(final @Nullable PlayerUser user) {
    if (user == null) {
      this.replyTarget((UUID) null, false);
    } else {
      this.replyTarget(user.uuid(), false);
    }
  }

  void replyTarget(@Nullable UUID target, boolean fromRemote);

  default void replyTarget(final @Nullable PlayerUser user, final boolean fromRemote) {
    if (user == null) {
      this.replyTarget((UUID) null, fromRemote);
    } else {
      this.replyTarget(user.uuid(), fromRemote);
    }
  }

  boolean ignoringUser(@NonNull UUID uuid);

  void ignoringUser(@NonNull UUID uuid, boolean ignoring, boolean fromRemote);

  default void ignoringUser(final @NonNull UUID uuid, final boolean ignoring) {
    this.ignoringUser(uuid, ignoring, false);
  }

  default boolean ignoringUser(final @NonNull PlayerUser user) {
    return this.ignoringUser(user.uuid());
  }

  default void ignoringUser(final @NonNull PlayerUser user, final boolean ignoring, final boolean fromRemote) {
    this.ignoringUser(user.uuid(), ignoring, fromRemote);
  }

  default void ignoringUser(final @NonNull PlayerUser user, final boolean ignoring) {
    this.ignoringUser(user.uuid(), ignoring, false);
  }

  @NonNull Iterable<CarbonUser> ignoredChatUsers();

  @NonNull Iterable<UUID> ignoredUsers();

  boolean hasGroup(final @NonNull String group);

  boolean hasGroup(final @NonNull Group group);

  @NonNull Collection<@NonNull Group> groups();

  @Nullable Group primaryGroup();

  void sendMessage(final @NonNull PlayerUser sender, @NonNull String message);

  @Nullable ChatChannel selectedChannel();

  default void selectedChannel(final @NonNull ChatChannel channel) {
    this.selectedChannel(channel, false);
  }

  void selectedChannel(@NonNull ChatChannel channel, boolean fromRemote);

  void clearSelectedChannel();

}
