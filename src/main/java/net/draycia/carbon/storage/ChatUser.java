package net.draycia.carbon.storage;

import net.draycia.carbon.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface ChatUser extends Audience {

    @Nullable Player asPlayer();
    @NonNull OfflinePlayer asOfflinePlayer();
    UUID getUUID();

    boolean isOnline();

    @Nullable String getNickname();
    void setNickname(String nickname, boolean fromRemote);
    default void setNickname(String nickname) {
        this.setNickname(nickname, false);
    }

    ChatChannel getSelectedChannel();
    void setSelectedChannel(ChatChannel channel, boolean fromRemote);
    default void setSelectedChannel(ChatChannel channel) {
        this.setSelectedChannel(channel, false);
    }

    void clearSelectedChannel();

    UserChannelSettings getChannelSettings(ChatChannel channel);

    boolean isSpyingWhispers();
    void setSpyingWhispers(boolean spyingWhispers, boolean fromRemote);
    default void setSpyingWhispers(boolean spyingWhispers) {
        this.setSpyingWhispers(spyingWhispers, false);
    }

    boolean isMuted();
    void setMuted(boolean muted, boolean fromRemote);
    default void setMuted(boolean muted) {
        this.setMuted(muted, false);
    }

    boolean isShadowMuted();
    void setShadowMuted(boolean shadowMuted, boolean fromRemote);
    default void setShadowMuted(boolean shadowMuted) {
        this.setShadowMuted(shadowMuted, false);
    }

    @Nullable @Nullable UUID getReplyTarget();
    void setReplyTarget(@Nullable UUID target, boolean fromRemote);
    default void setReplyTarget(@Nullable UUID target) {
        this.setReplyTarget(target, false);
    }

    default void setReplyTarget(@Nullable ChatUser user, boolean fromRemote) {
        this.setReplyTarget(user.getUUID(), fromRemote);
    }
    default void setReplyTarget(@Nullable ChatUser user) {
        this.setReplyTarget(user.getUUID(), false);
    }

    boolean isIgnoringUser(UUID uuid);
    void setIgnoringUser(UUID uuid, boolean ignoring, boolean fromRemote);
    default void setIgnoringUser(UUID uuid, boolean ignoring) {
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

    void sendMessage(ChatUser sender, String message);

}
