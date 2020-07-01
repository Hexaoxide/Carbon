package net.draycia.simplechat.storage;

import net.draycia.simplechat.channels.ChatChannel;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.CheckForNull;
import java.util.UUID;

public interface ChatUser {

    Audience asAudience();
    Player asPlayer();
    OfflinePlayer asOfflinePlayer();
    UUID getUUID();

    boolean isOnline();

    ChatChannel getSelectedChannel();
    void setSelectedChannel(ChatChannel channel);

    boolean ignoringChannel(ChatChannel chatChannel);
    void setIgnoringChannel(ChatChannel chatChannel, boolean ignoring);

    void setMuted(boolean muted);
    boolean isMuted();

    void setShadowMuted(boolean shadowMuted);
    boolean isShadowMuted();

    @CheckForNull
    UUID getReplyTarget();
    void setReplyTarget(UUID target);
    void setReplyTarget(ChatUser user);

    boolean isIgnoringUser(UUID uuid);
    boolean isIgnoringUser(ChatUser user);
    void setIgnoringUser(UUID uuid, boolean ignoring);
    void setIgnoringUser(ChatUser user, boolean ignoring);

}
