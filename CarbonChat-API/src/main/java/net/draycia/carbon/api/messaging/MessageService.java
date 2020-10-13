package net.draycia.carbon.api.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.api.users.CarbonUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MessageService {

  void registerUserMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull CarbonUser, @NonNull ByteArrayDataInput> listener);

  void registerUUIDMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener);

  void unregisterMessageListener(@NonNull final String key);

  void sendMessage(@NonNull final String key, @NonNull final UUID uuid, @NonNull final Consumer<@NonNull ByteArrayDataOutput> consumer);

}
