package net.draycia.carbon.api.users;

import net.draycia.carbon.api.channels.ChatChannel;
import net.kyori.adventure.sound.Sound;
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

  @NonNull String nickname();

  void nickname(@Nullable String nickname);

  @NonNull String displayName();

  void displayName(@Nullable String displayName);

  @NonNull String name();

  @Nullable TextColor customChatColor();

  void customChatColor(@Nullable TextColor customChatColor);

  @NonNull String parsePlaceholders(@NonNull String input);

  @NonNull UserChannelSettings channelSettings(@NonNull ChatChannel channel);

  @NonNull Map<@NonNull String, @NonNull ? extends UserChannelSettings> channelSettings();

  boolean spyingWhispers();

  void spyingWhispers(boolean spyingWhispers);

  boolean muted();

  void muted(boolean muted);

  boolean shadowMuted();

  void shadowMuted(boolean shadowMuted);

  @Nullable UUID replyTarget();

  default void replyTarget(final @Nullable PlayerUser user) {
    if (user == null) {
      this.replyTarget((UUID) null);
    } else {
      this.replyTarget(user.uuid());
    }
  }

  void replyTarget(@Nullable UUID target);

  boolean ignoringUser(@NonNull UUID uuid);

  void ignoringUser(@NonNull UUID uuid, boolean ignoring);

  default boolean ignoringUser(final @NonNull PlayerUser user) {
    return this.ignoringUser(user.uuid());
  }

  default void ignoringUser(final @NonNull PlayerUser user, boolean ignoring) {
    this.ignoringUser(user.uuid(), ignoring);
  }

  @NonNull Iterable<CarbonUser> ignoredChatUsers();

  @NonNull Iterable<UUID> ignoredUsers();

  boolean hasGroup(@NonNull String group);

  boolean hasGroup(@NonNull Group group);

  @NonNull Collection<@NonNull Group> groups();

  @Nullable Group primaryGroup();

  void sendMessage(@NonNull PlayerUser sender, @NonNull String message);

  @Nullable ChatChannel selectedChannel();

  void selectedChannel(@NonNull ChatChannel channel);

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
