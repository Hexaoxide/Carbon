package net.draycia.carbon.api.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlayerUser extends CarbonUser {

  @NonNull UUID uuid();

  boolean online();

  @NonNull Component nickname();

  default void nickname(final @Nullable Component nickname) {
    this.nickname(nickname, false);
  }

  void nickname(@Nullable Component nickname, boolean fromRemote);

  @NonNull Component displayName();

  void displayName(@Nullable Component displayName);

  @Nullable TextColor customChatColor();

  void customChatColor(@Nullable TextColor customChatColor, boolean fromRemote);

  @NonNull String parsePlaceholders(@NonNull String input);

  // TODO: change this
  // impl behaviour is to computeIfAbsent the channel key in the channelSettings
  // this doesn't really convey that nor is it documented
  @NonNull UserChannelSettings channelSettings(@NonNull ChatChannel channel);

  // TODO: also this here is kinda dumb
  // this only exists to easily enable whisper "channel" settings being saved on the player
  // really, this shouldn't exist and instead the player's whisper channel should be passed in with the correct key
  // but players don't always have a whisper channel, what if they've never whispered anyone?
  // and the whisper channels are discarded sometimes, so that's also an issue
  @NonNull UserChannelSettings channelSettings(@NonNull String key);

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

  boolean hasGroup(@NonNull String group);

  boolean hasGroup(@NonNull Group group);

  @NonNull Collection<@NonNull Group> groups();

  @Nullable Group primaryGroup();

  void sendMessage(@NonNull PlayerUser sender, @NonNull String message);

  @Nullable ChatChannel selectedChannel();

  default void selectedChannel(final @NonNull ChatChannel channel) {
    this.selectedChannel(channel, false);
  }

  void selectedChannel(@NonNull ChatChannel channel, boolean fromRemote);

  void clearSelectedChannel();

  @NonNull PingOptions pingOptions();

  void pingOptions(@NonNull PingOptions pingOptions);

  class PingOptions {
    public @Nullable Sound whisperSound;
    public @Nullable Sound pingSound;

    public PingOptions(final @Nullable Sound whisperSound, final @Nullable Sound pingSound) {
      this.whisperSound = whisperSound;
      this.pingSound = pingSound;
    }

    public @Nullable Sound whisperSound() {
      return this.whisperSound;
    }

    public void whisperSound(final @Nullable Sound whisperSound) {
      this.whisperSound = whisperSound;
    }

    public @Nullable Sound pingSound() {
      return this.pingSound;
    }

    public void pingSound(final @Nullable Sound pingSound) {
      this.pingSound = pingSound;
    }
  }

}
