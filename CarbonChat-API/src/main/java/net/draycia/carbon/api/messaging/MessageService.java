package net.draycia.carbon.api.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import net.draycia.carbon.api.users.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MessageService {

  void registerUserMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener);

  void registerUUIDMessageListener(final @NonNull String key, final @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener);

  void unregisterMessageListener(final @NonNull String key);

  void sendMessage(final @NonNull String key, final @NonNull UUID uuid, final @NonNull Consumer<@NonNull ByteArrayDataOutput> consumer);

}
