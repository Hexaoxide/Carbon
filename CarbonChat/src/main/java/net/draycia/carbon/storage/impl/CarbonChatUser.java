package net.draycia.carbon.storage.impl;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.events.ChannelSwitchEvent;
import net.draycia.carbon.events.PrivateMessageEvent;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.UserChannelSettings;
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

public class CarbonChatUser implements ChatUser, ForwardingAudience {

    private final transient CarbonChat carbonChat;

    private UUID uuid;

    private String selectedChannel = null;
    private final Map<String, SimpleUserChannelSettings> channelSettings = new HashMap<>();
    private final List<UUID> ignoredUsers = new ArrayList<>();
    private boolean muted = false;
    private boolean shadowMuted = false;
    private boolean spyingWhispers = false;

    private String nickname = null;

    private transient UUID replyTarget = null;

    public CarbonChatUser() {
        this.carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
    }

    public CarbonChatUser(UUID uuid) {
        this.carbonChat = (CarbonChat) Bukkit.getPluginManager().getPlugin("CarbonChat");
        this.uuid = uuid;
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        return Collections.singleton(carbonChat.getAdventureManager().getAudiences().player(uuid));
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
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname, boolean fromRemote) {
        if (nickname != null) {
            Component component = carbonChat.getAdventureManager().processMessage(nickname);
            nickname = CarbonChat.LEGACY.serialize(component);
        }

        this.nickname = nickname;

        if (isOnline()) {
            this.asPlayer().setDisplayName(nickname);
            this.asPlayer().setPlayerListName(nickname);
        }

        if (!fromRemote) {
            if (nickname == null) {
                this.publishChangeToRedis("nickname-reset", "true");
            } else {
                this.publishChangeToRedis("nickname", nickname);
            }
        }
    }

    @Override
    public ChatChannel getSelectedChannel() {
        return carbonChat.getChannelManager().getChannelOrDefault(selectedChannel);
    }

    @Override
    public void setSelectedChannel(ChatChannel chatChannel, boolean fromRemote) {
        String failureMessage = chatChannel.getSwitchFailureMessage();

        ChannelSwitchEvent event = new ChannelSwitchEvent(chatChannel, this, failureMessage);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            sendMessage(carbonChat.getAdventureManager().processMessage(event.getFailureMessage(),
                    "channel", chatChannel.getName()));

            return;
        }

        this.selectedChannel = chatChannel.getKey();

        if (!fromRemote) {
            this.publishChangeToRedis("selected-channel", chatChannel.getKey());
        }
    }

    @Override
    public void clearSelectedChannel() {
        setSelectedChannel(carbonChat.getChannelManager().getDefaultChannel());
    }

    @Override
    public boolean isIgnoringUser(UUID uuid) {
        return ignoredUsers.contains(uuid);
    }

    @Override
    public void setIgnoringUser(UUID uuid, boolean ignoring, boolean fromRemote) {
        if (ignoring) {
            if (!fromRemote) {
                this.publishChangeToRedis("ignoring-user", uuid.toString());
            }

            ignoredUsers.add(uuid);
        } else {
            if (!fromRemote) {
                this.publishChangeToRedis("unignoring-user", uuid.toString());
            }

            ignoredUsers.remove(uuid);
        }
    }

    public List<UUID> getIgnoredUsers() {
        return ignoredUsers;
    }

    @Override
    public void setMuted(boolean muted, boolean fromRemote) {
        this.muted = muted;

        if (!fromRemote) {
            this.publishChangeToRedis("muted", muted ? "true" : "false");
        }
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setShadowMuted(boolean shadowMuted, boolean fromRemote) {
        this.shadowMuted = shadowMuted;

        if (!fromRemote) {
            this.publishChangeToRedis("shadow-muted", shadowMuted ? "true" : "false");
        }
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
    public void setReplyTarget(UUID target, boolean fromRemote) {
        this.replyTarget = target;

        if (!fromRemote) {
            this.publishChangeToRedis("reply-target", target.toString());
        }
    }

    @Override
    public UserChannelSettings getChannelSettings(ChatChannel channel) {
        return channelSettings.computeIfAbsent(channel.getKey(), (name) -> {
            return new SimpleUserChannelSettings(uuid, channel.getKey());
        });
    }

    public Map<String, ? extends UserChannelSettings> getChannelSettings() {
        return channelSettings;
    }

    @Override
    public void setSpyingWhispers(boolean spyingWhispers, boolean fromRemote) {
        this.spyingWhispers = spyingWhispers;

        if (!fromRemote) {
            this.publishChangeToRedis("spying-whispers", spyingWhispers ? "true" : "false");
        }
    }

    @Override
    public boolean isSpyingWhispers() {
        return spyingWhispers;
    }

    public void sendMessage(ChatUser sender, String message) {
        if (isIgnoringUser(sender) || sender.isIgnoringUser(this)) {
            return;
        }

        String toPlayerFormat = carbonChat.getConfig().getString("language.message-to-other");
        String fromPlayerFormat = carbonChat.getConfig().getString("language.message-from-other");

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

        Component toPlayerComponent = carbonChat.getAdventureManager().processMessage(toPlayerFormat, "br", "\n",
                "message", message,
                "targetname", targetOfflineName, "sendername", senderOfflineName,
                "target", targetName, "sender", senderName);

        Component fromPlayerComponent = carbonChat.getAdventureManager().processMessage(fromPlayerFormat, "br", "\n",
                "message", message,
                "targetname", targetOfflineName, "sendername", senderOfflineName,
                "target", targetName, "sender", senderName);

        PrivateMessageEvent event = new PrivateMessageEvent(sender, this, toPlayerComponent, fromPlayerComponent, message);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        if (this.isOnline()) {
            if (sender.isOnline()) {
                sender.sendMessage(toPlayerComponent);

                if (sender.isShadowMuted()) {
                    return;
                }

                this.sendMessage(fromPlayerComponent);

                sender.setReplyTarget(this);
                this.setReplyTarget(sender);

                if (carbonChat.getConfig().getBoolean("pings.on-whisper")) {
                    Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
                    Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
                    float volume = (float) carbonChat.getConfig().getDouble("pings.volume");
                    float pitch = (float) carbonChat.getConfig().getDouble("pings.pitch");

                    this.playSound(Sound.of(key, source, volume, pitch));
                }
            }
        } else if (sender.isOnline()) {
            carbonChat.getPluginMessageManager().sendComponentToPlayer(sender, this, toPlayerComponent, fromPlayerComponent);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUser user = carbonChat.getUserService().wrap(player);

            if (!user.isSpyingWhispers()) {
                return;
            }

            if (user.getUUID().equals(sender.getUUID()) || user.getUUID().equals(getUUID())) {
                continue;
            }

            user.sendMessage(carbonChat.getAdventureManager().processMessage(carbonChat.getConfig().getString("language.spy-whispers"),
                    "br", "\n", "message", message,
                    "targetname", targetOfflineName, "sendername", senderOfflineName,
                    "target", targetName, "sender", senderName));
        }
    }

    /* package */ void publishChangeToRedis(String key, String value) {
        if (carbonChat.getRedisManager() == null) {
            return;
        }

        carbonChat.getRedisManager().publishChange(uuid, key, value);
    }
}
