package net.draycia.simplechat.managers;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.contexts.MessageContext;
import net.draycia.simplechat.channels.contexts.impl.SimpleMessageContext;
import net.draycia.simplechat.storage.ChatUser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ContextManager {

    private final Map<String, Function<MessageContext, Boolean>> handlers = new HashMap<>();

    public boolean register(String key, Function<MessageContext, Boolean> handler) {
        if (this.handlers.containsKey(key)) {
            return false;
        }

        this.handlers.put(key, handler);
        return true;
    }

    public boolean testContext(ChatUser user, ChatUser target, ChatChannel channel) {
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
