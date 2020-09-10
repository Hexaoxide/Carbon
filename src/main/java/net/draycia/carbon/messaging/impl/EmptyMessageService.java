package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmptyMessageService implements MessageService {

  @Override
  public void registerUserMessageListener(@NonNull String key, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener) {
    // do nothing
  }

  @Override
  public void registerUUIDMessageListener(@NonNull String key, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    // do nothing
  }

  @Override
  public void unregisterMessageListener(@NonNull String key) {
    // do nothing
  }

  @Override
  public void sendMessage(@NonNull String key, @NonNull UUID uuid, @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer) {
    // do nothing
  }

}
