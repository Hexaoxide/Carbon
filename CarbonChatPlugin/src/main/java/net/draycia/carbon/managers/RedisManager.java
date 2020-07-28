package net.draycia.carbon.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.storage.impl.CarbonChatUser;

import java.lang.reflect.Type;
import java.util.UUID;

public class RedisManager {

    private final RedisClient client;
    private final StatefulRedisPubSubConnection<String, String> connection;
    private final RedisPubSubCommands<String, String> sync;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type userType = new TypeToken<CarbonChatUser>() {}.getType();

    public RedisManager(CarbonChat carbonChat) {

        String host = carbonChat.getConfig().getString("redis.host");
        int port = carbonChat.getConfig().getInt("redis.port");
        int database = carbonChat.getConfig().getInt("redis.database");

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
                    carbonChat.getUserService().refreshUser(UUID.fromString(message));
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

    public void publishUser(CarbonChatUser user) {
        String data = gson.toJson(user, userType);

        sync.set(user.getUUID().toString(), data);
        sync.publish("refreshuser", user.getUUID().toString());
    }

    public CarbonChatUser getUser(UUID uuid) {
        String value = sync.get(uuid.toString());

        if (value == null) {
            return null;
        }

        return gson.fromJson(value, userType);
    }

}
