package net.draycia.simplechat.storage.impl;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleChatUser implements ChatUser {

    private SimpleChat simpleChat;
    private UUID uuid;

    private ChatChannel selectedChannel;
    private List<ChatChannel> ignoredChannels = new ArrayList<>();
    private List<UUID> ignoredUsers = new ArrayList<>();
    private boolean muted;
    private boolean shadowMuted;
    private UUID replyTarget = null;

    public SimpleChatUser(SimpleChat simpleChat, UUID uuid) {
        this.simpleChat = simpleChat;
        this.uuid = uuid;
    }

    @Override
    public Audience asAudience() {
        return simpleChat.getAudiences().player(uuid);
    }

    @Override
    public Player asPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public OfflinePlayer asOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public boolean isOnline() {
        return asOfflinePlayer().isOnline();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public ChatChannel getSelectedChannel() {
        return selectedChannel;
    }

    @Override
    public void setSelectedChannel(ChatChannel chatChannel) {
        this.selectedChannel = chatChannel;
    }

    @Override
    public boolean ignoringChannel(ChatChannel chatChannel) {
        return ignoredChannels.contains(chatChannel);
    }

    @Override
    public void setIgnoringChannel(ChatChannel chatChannel, boolean ignoring) {
        if (ignoring) {
            ignoredChannels.add(chatChannel);
        } else {
            ignoredChannels.remove(chatChannel);
        }
    }

    @Override
    public boolean isIgnoringUser(UUID uuid) {
        return ignoredUsers.contains(uuid);
    }

    @Override
    public boolean isIgnoringUser(ChatUser user) {
        return ignoredUsers.contains(user.getUUID());
    }

    @Override
    public void setIgnoringUser(UUID uuid, boolean ignoring) {
        if (ignoring) {
            ignoredUsers.add(uuid);
        } else {
            ignoredUsers.remove(uuid);
        }
    }

    @Override
    public void setIgnoringUser(ChatUser user, boolean ignoring) {
        if (ignoring) {
            ignoredUsers.add(user.getUUID());
        } else {
            ignoredUsers.remove(user.getUUID());
        }
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setShadowMuted(boolean shadowMuted) {
        this.shadowMuted = shadowMuted;
    }

    @Override
    public boolean isShadowMuted() {
        return shadowMuted;
    }

    @CheckForNull
    @Override
    public UUID getReplyTarget() {
        return replyTarget;
    }

    @Override
    public void setReplyTarget(UUID target) {
        this.replyTarget = target;
    }

    @Override
    public void setReplyTarget(ChatUser user) {
        this.replyTarget = user.getUUID();
    }

    public void sendMessage(ChatUser sender, String message) {
        if (isIgnoringUser(sender) || sender.isIgnoringUser(this)) {
            return;
        }

        String toPlayerFormat = simpleChat.getConfig().getString("language.message-to-other");
        String fromPlayerFormat = simpleChat.getConfig().getString("language.message-from-other");

        Component toPlayerComponent = MiniMessage.instance().parse(toPlayerFormat, "message", message,
                "target", this.asOfflinePlayer().getName());

        Component fromPlayerComponent = MiniMessage.instance().parse(fromPlayerFormat, "message", message,
                "sender", sender.asOfflinePlayer().getName());

        sender.asAudience().sendMessage(toPlayerComponent);

        if (sender.isShadowMuted()) {
            return;
        }

        if (this.isOnline()) {
            this.asAudience().sendMessage(fromPlayerComponent);

            sender.setReplyTarget(this.getUUID());
            this.setReplyTarget(sender.getUUID());

            if (simpleChat.getConfig().getBoolean("pings.on-whisper")) {
                Key key = Key.of(simpleChat.getConfig().getString("pings.sound"));
                Sound.Source source = Sound.Source.valueOf(simpleChat.getConfig().getString("pings.source"));
                float volume = (float)simpleChat.getConfig().getDouble("pings.volume");
                float pitch = (float)simpleChat.getConfig().getDouble("pings.pitch");

                this.asAudience().playSound(Sound.of(key, source, volume, pitch));
            }
        } else {
            // TODO: cross server msg support, don't forget to include /ignore support
        }
    }

}
