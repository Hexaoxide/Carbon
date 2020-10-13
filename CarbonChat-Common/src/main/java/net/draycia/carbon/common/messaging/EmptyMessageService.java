package net.draycia.carbon.common.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.api.messaging.MessageService;
import net.draycia.carbon.api.users.CarbonUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmptyMessageService implements MessageService {

  @Override
  public void registerUserMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull CarbonUser, @NonNull ByteArrayDataInput> listener) {
    // do nothing
  }

  @Override
  public void registerUUIDMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    // do nothing
  }

  @Override
  public void unregisterMessageListener(@NonNull final String key) {
    // do nothing
  }

  @Override
  public void sendMessage(@NonNull final String key, @NonNull final UUID uuid, @NonNull final Consumer<@NonNull ByteArrayDataOutput> consumer) {
    // do nothing
  }

}
