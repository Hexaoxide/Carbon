package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.RedisListener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RedisMessageService implements MessageService {

  @NonNull
  private final Map<
          @NonNull String, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput>>
      userLoadedListeners = new HashMap<>();

  @NonNull
  private final Map<
          @NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>>
      userNotLoadedListeners = new HashMap<>();

  @NonNull private final RedisPubSubCommands<@NonNull String, @NonNull String> subscribeSync;

  @NonNull private final RedisPubSubCommands<@NonNull String, @NonNull String> publishSync;

  @NonNull private final UUID serverUUID = UUID.randomUUID();

  @NonNull private final CarbonChat carbonChat;

  public RedisMessageService(@NonNull CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    String host = carbonChat.getConfig().getString("redis.host");
    String password = carbonChat.getConfig().getString("redis.password");
    int port = carbonChat.getConfig().getInt("redis.port");
    int database = carbonChat.getConfig().getInt("redis.database");

    RedisURI.Builder builder = RedisURI.Builder.redis(host, port).withDatabase(database);

    if (password != null) {
      builder.withPassword(password.toCharArray());
    }

    RedisClient client = RedisClient.create(builder.build());

    StatefulRedisPubSubConnection<String, String> subscribeConnection = client.connectPubSub();
    this.subscribeSync = subscribeConnection.sync();

    StatefulRedisPubSubConnection<String, String> publishConnection = client.connectPubSub();
    this.publishSync = publishConnection.sync();

    subscribeConnection.addListener(
        (RedisListener)
            (channel, message) -> {
              ByteArrayDataInput input =
                  ByteStreams.newDataInput(Base64.getDecoder().decode(message));

              UUID messageUUID = new UUID(input.readLong(), input.readLong());

              if (messageUUID.equals(serverUUID)) {
                return;
              }

              UUID uuid = new UUID(input.readLong(), input.readLong());

              this.receiveMessage(uuid, channel, input);
            });
  }

  private void receiveMessage(
      @NonNull UUID uuid, @NonNull String key, @NonNull ByteArrayDataInput value) {
    ChatUser user = carbonChat.getUserService().wrapIfLoaded(uuid);

    if (user != null) {
      for (Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener :
          userLoadedListeners.entrySet()) {
        if (key.equals(listener.getKey())) {
          listener.getValue().accept(user, value);
        }
      }
    }

    for (Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener :
        userNotLoadedListeners.entrySet()) {
      if (key.equals(listener.getKey())) {
        listener.getValue().accept(uuid, value);
      }
    }
  }

  @Override
  public void registerUserMessageListener(
      @NonNull String key,
      @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener) {
    userLoadedListeners.put(key, listener);
    subscribeSync.subscribe(key);
  }

  @Override
  public void registerUUIDMessageListener(
      @NonNull String key,
      @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    userNotLoadedListeners.put(key, listener);
    subscribeSync.subscribe(key);
  }

  @Override
  public void unregisterMessageListener(@NonNull String key) {
    userLoadedListeners.remove(key);
    userNotLoadedListeners.remove(key);

    subscribeSync.unsubscribe(key);
  }

  @Override
  public void sendMessage(
      @NonNull String key,
      @NonNull UUID uuid,
      @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer) {
    ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(serverUUID.getMostSignificantBits());
    msg.writeLong(serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    publishSync.publish(key, Base64.getEncoder().encodeToString(msg.toByteArray()));
  }
}
