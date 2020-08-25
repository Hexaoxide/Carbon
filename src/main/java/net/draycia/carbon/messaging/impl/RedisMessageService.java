package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.RedisListener;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RedisMessageService implements MessageService {

    private final Map<String, BiConsumer<ChatUser, ByteArrayDataInput>> userLoadedListeners = new HashMap<>();
    private final Map<String, BiConsumer<UUID, ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

    private final RedisPubSubCommands<String, String> subscribeSync;
    private final RedisPubSubCommands<String, String> publishSync;

    private final UUID serverUUID = UUID.randomUUID();
    private final CarbonChat carbonChat;

    public RedisMessageService(CarbonChat carbonChat) {
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

        RedisClient client = RedisClient.create(builder.build());

        StatefulRedisPubSubConnection<String, String> subscribeConnection = client.connectPubSub();
        this.subscribeSync = subscribeConnection.sync();

        StatefulRedisPubSubConnection<String, String> publishConnection = client.connectPubSub();
        this.publishSync = publishConnection.sync();

        subscribeConnection.addListener((RedisListener)(channel, message) -> {
            ByteArrayDataInput input = ByteStreams.newDataInput(Base64.getDecoder().decode(message));

            UUID messageUUID = new UUID(input.readLong(), input.readLong());

            if (messageUUID.equals(serverUUID)) {
                return;
            }

            UUID uuid = new UUID(input.readLong(), input.readLong());

            this.receiveMessage(uuid, channel, input);
        });
    }

    private void receiveMessage(UUID uuid, String key, ByteArrayDataInput value) {
        ChatUser user = carbonChat.getUserService().wrapIfLoaded(uuid);

        if (user != null) {
            for (Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener : userLoadedListeners.entrySet()) {
                if (key.equals(listener.getKey())) {
                    listener.getValue().accept(user, value);
                }
            }
        }

        for (Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener : userNotLoadedListeners.entrySet()) {
            if (key.equals(listener.getKey())) {
                listener.getValue().accept(uuid, value);
            }
        }
    }

    @Override
    public void registerUserMessageListener(String key, BiConsumer<ChatUser, ByteArrayDataInput> listener) {
        userLoadedListeners.put(key, listener);
        subscribeSync.subscribe(key);
    }

    @Override
    public void registerUUIDMessageListener(String key, BiConsumer<UUID, ByteArrayDataInput> listener) {
        userNotLoadedListeners.put(key, listener);
        subscribeSync.subscribe(key);
    }

    @Override
    public void unregisterMessageListener(String key) {
        userLoadedListeners.remove(key);
        userNotLoadedListeners.remove(key);

        subscribeSync.unsubscribe(key);
    }

    @Override
    public void sendMessage(String key, UUID uuid, Consumer<ByteArrayDataOutput> consumer) {
        ByteArrayDataOutput msg = ByteStreams.newDataOutput();

        msg.writeLong(serverUUID.getMostSignificantBits());
        msg.writeLong(serverUUID.getLeastSignificantBits());
        msg.writeLong(uuid.getMostSignificantBits());
        msg.writeLong(uuid.getLeastSignificantBits());

        consumer.accept(msg);

        publishSync.publish(key, Base64.getEncoder().encodeToString(msg.toByteArray()));
    }

}
