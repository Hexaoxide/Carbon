package net.draycia.carbon.common.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.config.RedisCredentials;
import net.draycia.carbon.api.users.PlayerUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RedisMessageService implements MessageService {

  private final @NonNull Map<@NonNull String, @NonNull BiConsumer<@NonNull PlayerUser, @NonNull ByteArrayDataInput>> userLoadedListeners = new HashMap<>();

  private final @NonNull Map<@NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

  private final @NonNull RedisPubSubCommands<@NonNull String, @NonNull String> subscribeSync;

  private final @NonNull RedisPubSubCommands<@NonNull String, @NonNull String> publishSync;

  private final @NonNull RedisStringCommands<@NonNull String, @NonNull String> getSetSync;

  private final @NonNull UUID serverUUID = UUID.randomUUID();

  private final @NonNull CarbonChat carbonChat;

  @SuppressWarnings("method.invocation.invalid")
  public RedisMessageService(final @NonNull CarbonChat carbonChat, final @NonNull RedisCredentials credentials) {
    this.carbonChat = carbonChat;

    final RedisURI.Builder builder = RedisURI.Builder.redis(credentials.host(), credentials.port());

    final Integer database = credentials.database();

    if (database != null) {
      builder.withDatabase(database);
    }

    final String password = credentials.password();

    if (password != null) {
      builder.withPassword(password.toCharArray());
    }

    final RedisClient client = RedisClient.create(builder.build());

    final StatefulRedisPubSubConnection<String, String> subscribeConnection = client.connectPubSub();
    this.subscribeSync = subscribeConnection.sync();

    final StatefulRedisPubSubConnection<String, String> publishConnection = client.connectPubSub();
    this.publishSync = publishConnection.sync();

    final StatefulRedisConnection<String, String> getSetConnection = client.connect();
    this.getSetSync = getSetConnection.sync();

    subscribeConnection.addListener((RedisListener) (channel, message) -> {
      final ByteArrayDataInput input = ByteStreams.newDataInput(Base64.getDecoder().decode(message));

      final UUID messageUUID = new UUID(input.readLong(), input.readLong());

      if (messageUUID.equals(this.serverUUID)) {
        return;
      }

      final UUID uuid = new UUID(input.readLong(), input.readLong());

      this.receiveMessage(uuid, channel, input);
    });
  }

  private void receiveMessage(final @NonNull UUID uuid, final @NonNull String key, final @NonNull ByteArrayDataInput value) {
    final PlayerUser user = this.carbonChat.userService().wrapIfLoaded(uuid);

    if (user != null) {
      for (final Map.Entry<String, BiConsumer<PlayerUser, ByteArrayDataInput>> listener : this.userLoadedListeners.entrySet()) {
        if (key.equals(listener.getKey())) {
          listener.getValue().accept(user, value);
        }
      }
    }

    for (final Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener : this.userNotLoadedListeners.entrySet()) {
      if (key.equals(listener.getKey())) {
        listener.getValue().accept(uuid, value);
      }
    }
  }

  @Override
  public void registerUserMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull PlayerUser, @NonNull ByteArrayDataInput> listener) {
    this.userLoadedListeners.put(key, listener);
    this.subscribeSync.subscribe(key);
  }

  @Override
  public void registerUUIDMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    this.userNotLoadedListeners.put(key, listener);
    this.subscribeSync.subscribe(key);
  }

  @Override
  public void unregisterMessageListener(final @NonNull String key) {
    this.userLoadedListeners.remove(key);
    this.userNotLoadedListeners.remove(key);

    this.subscribeSync.unsubscribe(key);
  }

  public @Nullable String get(final @NonNull String key) {
    return this.getSetSync.get(key);
  }

  public void set(final @NonNull String key, final @NonNull String value) {
    this.getSetSync.set(key, value);
  }

  @Override
  public void sendMessage(final @NonNull String key, final @NonNull UUID uuid, final @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer) {
    final ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(this.serverUUID.getMostSignificantBits());
    msg.writeLong(this.serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    this.publishSync.publish(key, Base64.getEncoder().encodeToString(msg.toByteArray()));
  }

}
