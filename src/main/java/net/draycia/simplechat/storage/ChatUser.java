package net.draycia.simplechat.storage;

import net.draycia.simplechat.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ChatUser extends Audience {

    Player asPlayer();
    OfflinePlayer asOfflinePlayer();
    UUID getUUID();

    boolean isOnline();

    ChatChannel getSelectedChannel();
    void setSelectedChannel(ChatChannel channel);

    void clearSelectedChannel();

    UserChannelSettings getChannelSettings(ChatChannel channel);

    void setSpyingWhispers(boolean spyingWhispers);
    boolean isSpyingWhispers();

    void setMuted(boolean muted);
    boolean isMuted();

    void setShadowMuted(boolean shadowMuted);
    boolean isShadowMuted();

    @Nullable UUID getReplyTarget();
    void setReplyTarget(@Nullable UUID target);
    void setReplyTarget(@Nullable ChatUser user);

    boolean isIgnoringUser(UUID uuid);
    boolean isIgnoringUser(ChatUser user);
    void setIgnoringUser(UUID uuid, boolean ignoring);
    void setIgnoringUser(ChatUser user, boolean ignoring);

    void sendMessage(ChatUser sender, String message);

}
