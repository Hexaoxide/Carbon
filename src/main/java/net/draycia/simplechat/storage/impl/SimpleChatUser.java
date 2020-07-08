package net.draycia.simplechat.storage.impl;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.CheckForNull;
import java.util.*;

public class SimpleChatUser implements ChatUser, ForwardingAudience {

    private transient SimpleChat simpleChat;

    private UUID uuid;

    private String selectedChannel = null;
    private Map<String, SimpleUserChannelSettings> channelSettings = new HashMap<>();
    private List<UUID> ignoredUsers = new ArrayList<>();
    private boolean muted = false;
    private boolean shadowMuted = false;

    private transient UUID replyTarget = null;

    public SimpleChatUser() {
        this.simpleChat = (SimpleChat)Bukkit.getPluginManager().getPlugin("SimpleChat");
    }

    public SimpleChatUser(UUID uuid) {
        this.simpleChat = (SimpleChat)Bukkit.getPluginManager().getPlugin("SimpleChat");
        this.uuid = uuid;
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        return Collections.singleton(simpleChat.getAudiences().player(uuid));
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
        return selectedChannel == null ? simpleChat.getDefaultChannel() : simpleChat.getChannel(selectedChannel);
    }

    @Override
    public void setSelectedChannel(ChatChannel chatChannel) {
        this.selectedChannel = chatChannel.getName();
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

    public List<UUID> getIgnoredUsers() {
        return ignoredUsers;
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

    @Override
    public UserChannelSettings getChannelSettings(ChatChannel channel) {
        return channelSettings.computeIfAbsent(channel.getName(), (name) -> new SimpleUserChannelSettings());
    }

    public Map<String, ? extends UserChannelSettings> getChannelSettings() {
        return channelSettings;
    }

    public void sendMessage(ChatUser sender, String message) {
        if (isIgnoringUser(sender) || sender.isIgnoringUser(this)) {
            return;
        }

        String toPlayerFormat = simpleChat.getConfig().getString("language.message-to-other");
        String fromPlayerFormat = simpleChat.getConfig().getString("language.message-from-other");

        Component toPlayerComponent = MiniMessage.get().parse(toPlayerFormat, "message", message,
                "target", this.asOfflinePlayer().getName());

        Component fromPlayerComponent = MiniMessage.get().parse(fromPlayerFormat, "message", message,
                "sender", sender.asOfflinePlayer().getName());

        if (this.isOnline()) {
            if (sender.isOnline()) {
                sender.sendMessage(toPlayerComponent);

                if (sender.isShadowMuted()) {
                    return;
                }

                this.sendMessage(fromPlayerComponent);

                sender.setReplyTarget(this);
                this.setReplyTarget(sender);

                if (simpleChat.getConfig().getBoolean("pings.on-whisper")) {
                    Key key = Key.of(simpleChat.getConfig().getString("pings.sound"));
                    Sound.Source source = Sound.Source.valueOf(simpleChat.getConfig().getString("pings.source"));
                    float volume = (float)simpleChat.getConfig().getDouble("pings.volume");
                    float pitch = (float)simpleChat.getConfig().getDouble("pings.pitch");

                    this.playSound(Sound.of(key, source, volume, pitch));
                }
            }
        } else if (sender.isOnline()) {
            simpleChat.getPluginMessageManager().sendComponentToPlayer(sender, this, toPlayerComponent, fromPlayerComponent);
        }
    }

}
