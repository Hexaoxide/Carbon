package net.draycia.simplechat.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.simplechat.SimpleChat;
import net.draycia.simplechat.storage.impl.SimpleChatUser;

import java.lang.reflect.Type;
import java.util.UUID;

public class RedisManager {

    private SimpleChat simpleChat;

    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;
    private RedisPubSubCommands<String, String> sync;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Type userType = new TypeToken<SimpleChatUser>() {}.getType();

    public RedisManager(SimpleChat simpleChat) {
        this.simpleChat = simpleChat;

        String host = simpleChat.getConfig().getString("redis.host");
        int port = simpleChat.getConfig().getInt("redis.port");
        int database = simpleChat.getConfig().getInt("redis.database");

        RedisURI uri = RedisURI.Builder.redis(host, port)
                .withDatabase(database)
                .build();

        this.client = RedisClient.create(uri);
        this.connection = client.connectPubSub();
        this.sync = connection.sync();

        connection.addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String channel, String message) {
                if (channel.equalsIgnoreCase("refreshuser")) {
                    simpleChat.getUserService().refreshUser(UUID.fromString(message));
                }
            }

            @Override
            public void message(String pattern, String channel, String message) { }

            @Override
            public void subscribed(String channel, long count) { }

            @Override
            public void psubscribed(String pattern, long count) { }

            @Override
            public void unsubscribed(String channel, long count) { }

            @Override
            public void punsubscribed(String pattern, long count) { }
        });

        sync.subscribe("userdata");
    }

    public void publishUser(SimpleChatUser user) {
        String data = gson.toJson(user, userType);

        sync.set(user.getUUID().toString(), data);
        sync.publish("refreshuser", user.getUUID().toString());
    }

    public SimpleChatUser getUser(UUID uuid) {
        String value = sync.get(uuid.toString());

        if (value == null) {
            return null;
        }

        return gson.fromJson(value, userType);
    }

}
