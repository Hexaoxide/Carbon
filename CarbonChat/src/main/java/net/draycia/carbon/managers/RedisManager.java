package net.draycia.carbon.managers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.RedisListener;
import net.kyori.adventure.text.format.TextColor;

import java.util.UUID;

public class RedisManager {

    private final CarbonChat carbonChat;

    private final RedisClient client;
    private final StatefulRedisPubSubConnection<String, String> connection;
    private final RedisPubSubCommands<String, String> sync;

    public RedisManager(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;

        String host = carbonChat.getConfig().getString("redis.host");
        String password = carbonChat.getConfig().getString("redis.password");
        int port = carbonChat.getConfig().getInt("redis.port");
        int database = carbonChat.getConfig().getInt("redis.database");

        RedisURI.Builder builder = RedisURI.Builder.redis(host, port)
                .withDatabase(database);

        if (password != null) {
            builder.withPassword(password.toCharArray());
        }

        this.client = RedisClient.create(builder.build());
        this.connection = client.connectPubSub();
        this.sync = connection.sync();

        connection.addListener((RedisListener)(channel, message) -> {
            UUID uuid = UUID.fromString(message.split(":", 2)[0]);

            ChatUser user = carbonChat.getUserService().wrapIfLoaded(uuid);

            if (user == null) {
                return;
            }

            String value = message.split(":", 2)[1];

            switch (channel.toLowerCase()) {
                case "nickname":
                    handleNicknameChange(user, value);
                    break;
                case "selected-channel":
                    handleSelectedChannelChange(user, value);
                    break;
                case "spying-whispers":
                    handleSpyingWhispersChange(user, value);
                    break;
                case "muted":
                    handleMutedChange(user, value);
                    break;
                case "shadow-muted":
                    handleShadowMutedChange(user, value);
                    break;
                case "reply-target":
                    handleReplyTargetChange(user, value);
                    break;
                case "ignoring-user":
                    handleIgnoringUserChange(user, value, true);
                    break;
                case "unignoring-user":
                    handleIgnoringUserChange(user, value, false);
                    break;
                case "ignoring-channel":
                    handleIgnoringChannelChange(user, value, true);
                    break;
                case "unignoring-channel":
                    handleIgnoringChannelChange(user, value, false);
                    break;
                case "spying-channel":
                    handleSpyingChannelChange(user, value, true);
                    break;
                case "unspying-channel":
                    handleSpyingChannelChange(user, value, false);
                    break;
                case "channel-color":
                    handleChannelColorChange(user, value);
                    break;
            }
        });

        sync.subscribe("nickname", "selected-channel", "spying-whispers", "muted", "shadow-muted",
                "reply-target", "ignoring-user", "unignoring-user", "ignoring-channel", "unignoring-channel",
                "spying-channel", "unspying-channel", "channel-color");
    }

    private void handleChannelColorChange(ChatUser user, String value) {
        user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(value)).setColor(TextColor.fromHexString(value));
    }

    private void handleSpyingChannelChange(ChatUser user, String value, boolean b) {
        user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(value)).setSpying(b);
    }

    private void handleIgnoringChannelChange(ChatUser user, String value, boolean b) {
        user.getChannelSettings(carbonChat.getChannelManager().getRegistry().get(value)).setIgnoring(b);
    }

    private void handleIgnoringUserChange(ChatUser user, String value, boolean b) {
        user.setIgnoringUser(UUID.fromString(value), b);
    }

    private void handleReplyTargetChange(ChatUser user, String value) {
        user.setReplyTarget(UUID.fromString(value));
    }

    private void handleShadowMutedChange(ChatUser user, String value) {
        user.setShadowMuted(value.equalsIgnoreCase("true"));
    }

    private void handleMutedChange(ChatUser user, String value) {
        user.setMuted(value.equalsIgnoreCase("true"));
    }

    private void handleSpyingWhispersChange(ChatUser user, String value) {
        user.setSpyingWhispers(value.equalsIgnoreCase("true"));
    }

    private void handleSelectedChannelChange(ChatUser user, String value) {
        user.setSelectedChannel(carbonChat.getChannelManager().getRegistry().get(value));
    }

    private void handleNicknameChange(ChatUser user, String value) {
        user.setNickname(value);
    }

    public void publishChange(UUID uuid, String field, String value) {
        sync.publish(field, uuid.toString() + ":" + value);
    }
}
