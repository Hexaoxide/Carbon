package net.draycia.carbon.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.contexts.MessageContext;
import net.draycia.carbon.channels.contexts.impl.SimpleMessageContext;
import net.draycia.carbon.storage.ChatUser;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ContextManager {

  @NonNull
  private final Map<@NonNull String, @NonNull Function<@NonNull MessageContext, @NonNull Boolean>>
      handlers = new HashMap<>();

  public boolean register(
      @NonNull String key, @NonNull Function<@NonNull MessageContext, @NonNull Boolean> handler) {
    if (this.handlers.containsKey(key)) {
      return false;
    }

    this.handlers.put(key, handler);
    return true;
  }

  public boolean testContext(
      @NonNull ChatUser user, @NonNull ChatUser target, @NonNull ChatChannel channel) {
    for (Map.Entry<String, Function<MessageContext, Boolean>> handler : handlers.entrySet()) {
      String key = handler.getKey();

      Object value = channel.getContext(key);

      if (value == null) {
        continue;
      }

      MessageContext context = new SimpleMessageContext(user, target, channel, value);

      return handler.getValue().apply(context);
    }

    return true;
  }
}
