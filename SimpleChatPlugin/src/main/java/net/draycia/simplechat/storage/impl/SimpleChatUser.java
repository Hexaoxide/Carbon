package net.draycia.simplechat.storage.impl;

import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.events.ChannelSwitchEvent;
import net.draycia.simplechat.events.PrivateMessageEvent;
import net.draycia.simplechat.storage.ChatUser;
import net.draycia.simplechat.storage.UserChannelSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
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
    private boolean spyingWhispers = false;

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
        syncToRedis();

        Bukkit.getPluginManager().callEvent(new ChannelSwitchEvent(chatChannel, this));
    }

    @Override
    public void clearSelectedChannel() {
        setSelectedChannel(simpleChat.getDefaultChannel());
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

        syncToRedis();
    }

    @Override
    public void setIgnoringUser(ChatUser user, boolean ignoring) {
        setIgnoringUser(user.getUUID(), ignoring);
    }

    public List<UUID> getIgnoredUsers() {
        return ignoredUsers;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;

        syncToRedis();
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setShadowMuted(boolean shadowMuted) {
        this.shadowMuted = shadowMuted;

        syncToRedis();
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

        syncToRedis();
    }

    @Override
    public void setReplyTarget(ChatUser user) {
        setReplyTarget(user.getUUID());
    }

    @Override
    public UserChannelSettings getChannelSettings(ChatChannel channel) {
        return channelSettings.computeIfAbsent(channel.getName(), (name) -> new SimpleUserChannelSettings());
    }

    public Map<String, ? extends UserChannelSettings> getChannelSettings() {
        return channelSettings;
    }

    @Override
    public void setSpyingWhispers(boolean spyingWhispers) {
        this.spyingWhispers = spyingWhispers;
    }

    @Override
    public boolean isSpyingWhispers() {
        return spyingWhispers;
    }

    public void sendMessage(ChatUser sender, String message) {
        if (isIgnoringUser(sender) || sender.isIgnoringUser(this)) {
            return;
        }

        String toPlayerFormat = simpleChat.getConfig().getString("language.message-to-other");
        String fromPlayerFormat = simpleChat.getConfig().getString("language.message-from-other");

        String senderName = sender.asOfflinePlayer().getName();
        String senderOfflineName = senderName;

        String targetName = this.asOfflinePlayer().getName();
        String targetOfflineName = targetName;

        if (sender.isOnline()) {
            senderName = sender.asPlayer().getDisplayName();
        }

        if (this.isOnline()) {
            targetName = this.asPlayer().getDisplayName();
        }

        Component toPlayerComponent = simpleChat.processMessage(toPlayerFormat,  "br", "\n",
                "message", message,
                "targetname", targetOfflineName, "sendername", senderOfflineName,
                "target", targetName, "sender", senderName);

        Component fromPlayerComponent = simpleChat.processMessage(fromPlayerFormat,  "br", "\n",
                "message", message,
                "targetname", targetOfflineName, "sendername", senderOfflineName,
                "target", targetName, "sender", senderName);

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

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUser user = simpleChat.getUserService().wrap(player);

            if (user.getUUID().equals(sender.getUUID()) || user.getUUID().equals(getUUID())) {
                continue;
            }

            user.sendMessage(simpleChat.processMessage(simpleChat.getConfig().getString("language.spy-whispers"),  "br", "\n",
                    "message", message,
                    "targetname", targetOfflineName, "sendername", senderOfflineName,
                    "target", targetName, "sender", senderName));
        }

        Bukkit.getPluginManager().callEvent(new PrivateMessageEvent(sender, this, toPlayerComponent, fromPlayerComponent, message));
    }

    private void syncToRedis() {
        if (simpleChat.getRedisManager() != null) {
            simpleChat.getRedisManager().publishUser(this);
        }
    }

}
