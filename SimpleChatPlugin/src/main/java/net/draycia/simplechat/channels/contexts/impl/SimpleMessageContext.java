package net.draycia.simplechat.channels.contexts.impl;

import net.draycia.simplechat.channels.ChatChannel;
import net.draycia.simplechat.channels.contexts.MessageContext;
import net.draycia.simplechat.storage.ChatUser;

public class SimpleMessageContext implements MessageContext {
    private final ChatUser sender;
    private final ChatUser target;
    private final ChatChannel chatChannel;
    private final Object value;

    public SimpleMessageContext(ChatUser sender, ChatUser target, ChatChannel chatChannel, Object value) {
        this.sender = sender;
        this.target = target;
        this.chatChannel = chatChannel;
        this.value = value;
    }

    @Override
    public ChatUser getSender() {
        return sender;
    }

    @Override
    public ChatUser getTarget() {
        return target;
    }

    @Override
    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
