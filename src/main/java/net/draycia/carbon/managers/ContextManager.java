package net.draycia.carbon.managers;

import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.contexts.MessageContext;
import net.draycia.carbon.channels.contexts.impl.SimpleMessageContext;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ContextManager {

  @NonNull
  private final Map<@NonNull String, @NonNull Function<@NonNull MessageContext, @NonNull Boolean>> handlers = new HashMap<>();

  public boolean register(@NonNull final String key, @NonNull final Function<@NonNull MessageContext, @NonNull Boolean> handler) {
    if (this.handlers.containsKey(key)) {
      return false;
    }

    this.handlers.put(key, handler);
    return true;
  }

  public boolean testContext(@NonNull final ChatUser user, @NonNull final ChatUser target, @NonNull final ChatChannel channel) {
    for (final Map.Entry<String, Function<MessageContext, Boolean>> handler : this.handlers.entrySet()) {
      final String key = handler.getKey();

      final Object value = channel.context(key);

      if (value == null) {
        continue;
      }

      final MessageContext context = new SimpleMessageContext(user, target, channel, value);

      return handler.getValue().apply(context);
    }

    return true;
  }

}
