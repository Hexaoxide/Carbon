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
    public void setNickname(String newNickname, boolean fromRemote) {
        this.nickname = newNickname;

        if (isOnline()) {
            if (newNickname != null) {
                Component component = carbonChat.getAdventureManager().processMessage(nickname);
                newNickname = CarbonChat.LEGACY.serialize(component);
            }

            this.asPlayer().setDisplayName(newNickname);

            if (carbonChat.getConfig().getBoolean("nicknames-set-tab-name")) {
                this.asPlayer().setPlayerListName(newNickname);
            }
        }

        if (!fromRemote) {
            if (nickname == null) {
                carbonChat.getMessageManager().sendMessage("nickname-reset", this.getUUID(), (byteArray) -> {});
            } else {
                carbonChat.getMessageManager().sendMessage("nickname", this.getUUID(), (byteArray) -> {
                    byteArray.writeUTF(this.nickname);
                });
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
            carbonChat.getMessageManager().sendMessage("selected-channel", this.getUUID(), (byteArray) -> {
                byteArray.writeUTF(chatChannel.getKey());
            });
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
            ignoredUsers.add(uuid);
        } else {
            ignoredUsers.remove(uuid);
        }

        if (!fromRemote) {
            carbonChat.getMessageManager().sendMessage("ignoring-user", this.getUUID(), (byteArray) -> {
                byteArray.writeLong(uuid.getMostSignificantBits());
                byteArray.writeLong(uuid.getLeastSignificantBits());
                byteArray.writeBoolean(ignoring);
            });
        }
    }

    public List<UUID> getIgnoredUsers() {
        return ignoredUsers;
    }

    @Override
    public void setMuted(boolean muted, boolean fromRemote) {
        this.muted = muted;

        if (!fromRemote) {
            carbonChat.getMessageManager().sendMessage("muted", this.getUUID(), (byteArray) -> {
                byteArray.writeBoolean(muted);
            });
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
            carbonChat.getMessageManager().sendMessage("shadow-muted", this.getUUID(), (byteArray) -> {
                byteArray.writeBoolean(shadowMuted);
            });
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
            carbonChat.getMessageManager().sendMessage("reply-target", this.getUUID(), (byteArray) -> {
                byteArray.writeLong(target.getMostSignificantBits());
                byteArray.writeLong(target.getLeastSignificantBits());
            });
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
            carbonChat.getMessageManager().sendMessage("spying-whispers", this.getUUID(), (byteArray) -> {
                byteArray.writeBoolean(spyingWhispers);
            });
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

        String toPlayerFormat = carbonChat.getLanguage().getString("message-to-other");
        String fromPlayerFormat = carbonChat.getLanguage().getString("message-from-other");

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
            sender.sendMessage(toPlayerComponent);

            // TODO: make sure this works lol
            carbonChat.getMessageManager().sendMessage("whisper-component", sender.getUUID(), (byteArray) -> {
                byteArray.writeLong(this.getUUID().getMostSignificantBits());
                byteArray.writeLong(this.getUUID().getLeastSignificantBits());
                byteArray.writeUTF(carbonChat.getAdventureManager().getAudiences().gsonSerializer().serialize(fromPlayerComponent));
            });
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            ChatUser user = carbonChat.getUserService().wrap(player);

            if (!user.isSpyingWhispers()) {
                continue;
            }

            if (user.getUUID().equals(sender.getUUID()) || user.getUUID().equals(getUUID())) {
                continue;
            }

            user.sendMessage(carbonChat.getAdventureManager().processMessage(carbonChat.getLanguage().getString("spy-whispers"),
                    "br", "\n", "message", message,
                    "targetname", targetOfflineName, "sendername", senderOfflineName,
                    "target", targetName, "sender", senderName));
        }
    }
}
