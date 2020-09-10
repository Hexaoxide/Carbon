package net.draycia.carbon.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessageService {

  void registerUserMessageListener(
      @NonNull String key,
      @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener);

  void registerUUIDMessageListener(
      @NonNull String key,
      @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener);

  void unregisterMessageListener(@NonNull String key);

  void sendMessage(
      @NonNull String key,
      @NonNull UUID uuid,
      @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer);
}
